const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserPayload(uid, now) {
  return {
    userId: uid,
    displayName: "Alice Khan",
    email: "alice@example.com",
    planTier: "FREE",
    vipExpiresAtMs: 0,
    dailyCreditsRemaining: 5,
    bonusCredits: 2,
    streakCount: 1,
    lastCheckInDate: "2026-11-20",
    referralCode: "LUMALICE",
    referralCount: 0,
    preferredCurrency: "PKR",
    preferredLanguage: "EN",
    darkThemeEnabled: true,
    notificationsEnabled: true,
    createdAt: now,
    updatedAt: now,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) await testEnv.cleanup();
});

beforeEach(async () => {
  if (testEnv) await testEnv.clearFirestore();
});

test("Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("challengeSubmissions").where("visibility", "==", "PUBLIC").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID, now)));
});

test("User profile isolation, PII protection, and self-upgrade prevention", async () => {
  const now = new Date(Date.now() - 2000);
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID, now)));

  // Bob cannot read Alice's profile (PII email) or list /users
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").get());

  // Alice can update her own theme and credits
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      darkThemeEnabled: false,
      dailyCreditsRemaining: 4,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Alice cannot self-grant VIP_YEARLY without admin approval
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      planTier: "VIP_YEARLY",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("User projects subcollection (My Work) enforces strict ownership and no @DocumentId id key", async () => {
  const now = new Date(Date.now() - 2000);
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_1").set({
      userId: ALICE_UID,
      title: "Lahore Night HDR",
      mediaType: "PHOTO",
      folderName: "Favorites",
      favorite: true,
      toolUsed: "Night Mode",
      filterName: "Cinematic",
      qualityLabel: "4K",
      enhancementSummary: "Smart HDR + Denoise",
      createdAt: now,
      updatedAt: now,
    })
  );

  // List query on own subcollection succeeds
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("projects").get());
  // Bob cannot read Alice's projects
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("projects").get());
  // Reject extra `id` field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("projects").doc("proj_2").set({
      id: "proj_2",
      userId: ALICE_UID,
      title: "Bad ID",
      mediaType: "PHOTO",
      folderName: "All",
      favorite: false,
      toolUsed: "Enhance",
      filterName: "Vivid",
      qualityLabel: "HD",
      enhancementSummary: "Auto",
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("EasyPaisa/JazzCash paymentRequests: user creates PENDING, only verified Admin can approve", async () => {
  const now = new Date(Date.now() - 2000);
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const spoofAdminDb = testEnv.authenticatedContext("spoof", { email: "hassankulachi53@gmail.com", email_verified: false }).firestore();
  const realAdminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "hassankulachi53@gmail.com", email_verified: true }).firestore();

  await assertSucceeds(
    aliceDb.collection("paymentRequests").doc("pay_1").set({
      userId: ALICE_UID,
      userEmail: "alice@example.com",
      userName: "Alice",
      paymentMethod: "EASYPAISA",
      planId: "VIP_MONTHLY",
      amountDisplay: "500 PKR",
      transactionRef: "EP-8839201",
      senderPhone: "03001234567",
      proofSummary: "screenshot_ep_8839.png",
      status: "PENDING",
      adminNote: "",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice can query her own payment requests
  await assertSucceeds(aliceDb.collection("paymentRequests").where("userId", "==", ALICE_UID).get());
  // Unfiltered query by non-admin fails
  await assertFails(aliceDb.collection("paymentRequests").get());
  // Alice cannot self-approve
  await assertFails(
    aliceDb.collection("paymentRequests").doc("pay_1").update({
      status: "APPROVED",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  // Spoofed unverified admin email fails
  await assertFails(
    spoofAdminDb.collection("paymentRequests").doc("pay_1").update({
      status: "APPROVED",
      adminNote: "Spoof",
      updatedAt: new Date(Date.now() - 500),
    })
  );
  // Verified admin succeeds
  await assertSucceeds(
    realAdminDb.collection("paymentRequests").doc("pay_1").update({
      status: "APPROVED",
      adminNote: "Verified EasyPaisa TID",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Daily Photo Challenge submissions: public showcase query, voting, and admin feature toggle", async () => {
  const now = new Date(Date.now() - 2000);
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const realAdminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "hassankulachi53@gmail.com", email_verified: true }).firestore();

  await assertSucceeds(
    aliceDb.collection("challengeSubmissions").doc("sub_1").set({
      userId: ALICE_UID,
      authorName: "Alice",
      dateKey: "2026-11-20",
      themeTitle: "City Nights",
      caption: "Clifton lights in HDR",
      toolUsed: "Night Mode",
      creativityScore: 92,
      qualityScore: 95,
      judgeFeedback: "Crisp neon reflections with balanced shadows.",
      prizeAwarded: "VIP_DAY_PASS",
      votesCount: 0,
      featured: false,
      visibility: "PUBLIC",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Public filtered query succeeds; unfiltered query fails
  await assertSucceeds(
    bobDb.collection("challengeSubmissions").where("visibility", "==", "PUBLIC").get()
  );
  await assertFails(bobDb.collection("challengeSubmissions").get());

  // Bob can upvote (+1) but cannot change caption or votesCount by +50
  await assertSucceeds(
    bobDb.collection("challengeSubmissions").doc("sub_1").update({
      votesCount: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    bobDb.collection("challengeSubmissions").doc("sub_1").update({
      votesCount: 51,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Admin can feature a top submission
  await assertSucceeds(
    realAdminDb.collection("challengeSubmissions").doc("sub_1").update({
      featured: true,
      updatedAt: new Date(Date.now() - 400),
    })
  );
});
