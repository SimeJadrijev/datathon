export const eligibilityKeys = {
    all: ["eligibility"] as const,
    questions: (fundingCallId: number) => [...eligibilityKeys.all, "questions", fundingCallId] as const,
};
