"use client";

import { useState } from "react";
import Link from "next/link";
import {
    ArrowLeft,
    Flag,
    Download,
    AlertCircle,
    CheckCircle2,
    Info,
    Terminal, // NEU
} from "lucide-react";
import { apiClient } from "@/lib/api/client";
import type { Challenge } from "@/lib/types";
import KaliTerminal from "@/components/KaliTerminal"; // NEU

interface ChallengeDetailProps {
    challenge: Challenge;
}

export default function ChallengeDetail({ challenge }: ChallengeDetailProps) {
    const [flag, setFlag] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [downloading, setDownloading] = useState(false);
    const [showTerminal, setShowTerminal] = useState(false); // NEU
    const [result, setResult] = useState<{
        status: string;
        message: string;
    } | null>(null);
    const [solved, setSolved] = useState<boolean>(!!challenge.solved);

    const [environment, setEnvironment] = useState(null);

    async function handleLaunchTerminal() {
        const res = await apiClient.post(`/api/environment/start/${challenge.id}`);
        setEnvironment(res);
        setShowTerminal(true);
    }

    // ... (existing handleSubmit and handleDownload functions bleiben gleich)

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        setResult(null);

        try {
            const response = await apiClient.post<{
                status: string;
                message: string;
            }>("/api/flags/submit", { challengeId: challenge.id, flag });

            setResult({
                status: response?.status ?? "error",
                message: response?.message ?? "",
            });

            if (response?.status === "success") {
                setFlag("");
                setSolved(true);
            }
        } catch (error) {
            setResult({
                status: "error",
                message: error instanceof Error ? error.message : "An error occurred",
            });
        } finally {
            setSubmitting(false);
        }
    };

    const handleDownload = async () => {
        if (!challenge.id) return;

        setDownloading(true);
        try {
            const challengeDetails = await apiClient.get(
                `/api/challenges/${challenge.id}`
            );
            const originalFilename = challengeDetails.originalFilename || challenge.id;

            const response = await fetch(
                `${
                    process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080"
                }/api/challenges/${challenge.id}/download`,
                {
                    method: "GET",
                    credentials: "include",
                }
            );

            if (!response.ok) {
                throw new Error(`Download failed: ${response.status}`);
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement("a");
            a.href = url;
            a.download = originalFilename;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            document.body.removeChild(a);
        } catch (error) {
            console.error("Download error:", error);
            setResult({
                status: "error",
                message: `Download failed: ${
                    error instanceof Error ? error.message : "Unknown error"
                }`,
            });
        } finally {
            setDownloading(false);
        }
    };

    const difficultyColors = {
        easy: "text-green-500 bg-green-500/10",
        medium: "text-yellow-500 bg-yellow-500/10",
        hard: "text-red-500 bg-red-500/10",
    };

    return (
        <div>
            <Link
                href="/challenges"
                className="inline-flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors mb-6"
            >
                <ArrowLeft className="w-4 h-4" />
                Back to Challenges
            </Link>

            <div className="bg-card p-8 rounded-lg border border-border">
                <div className="flex flex-wrap items-start justify-between gap-4 mb-6">
                    <div>
                        <h1 className="text-3xl font-bold mb-2">{challenge.title}</h1>
                        <div className="flex flex-wrap gap-3">
              <span
                  className={`text-sm font-semibold px-3 py-1 rounded-full ${
                      difficultyColors[challenge.difficulty]
                  }`}
              >
                {challenge.difficulty.toUpperCase()}
              </span>
                            <span className="text-sm px-3 py-1 bg-primary/10 text-primary rounded-full">
                {challenge.category
                    .split("-")
                    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
                    .join(" ")}
              </span>
                        </div>
                    </div>
                    <div className="text-right">
                        <div className="text-3xl font-bold text-accent">
                            {challenge.points}
                        </div>
                        <div className="text-sm text-muted-foreground">points</div>
                    </div>
                </div>

                <div className="mb-8">
                    <h2 className="text-xl font-semibold mb-3">Description</h2>
                    <p className="text-muted-foreground mb-6">{challenge.description}</p>

                    {/* Download Section */}
                    <div className="mb-6">
                        <button
                            onClick={handleDownload}
                            disabled={downloading}
                            className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <Download className="w-4 h-4" />
                            {downloading ? "Downloading..." : "Download Challenge Files"}
                        </button>
                        <p className="text-sm text-muted-foreground mt-2">
                            Download the challenge files to get started
                        </p>
                    </div>

                    {/* 🆕 TERMINAL SECTION - NEU HINZUGEFÜGT */}
                    <div className="mb-6">
                        <button
                            onClick={() => setShowTerminal(true)}
                            className="flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                        >
                            <Terminal className="w-4 h-4" />
                            🐉 Launch Kali Linux Terminal
                        </button>
                        <p className="text-sm text-muted-foreground mt-2">
                            Open a Kali Linux terminal with pentesting tools (nmap, curl,
                            sqlmap, etc.)
                        </p>
                    </div>

                    {challenge.hints?.length > 0 && (
                        <div className="mb-6">
                            <h3 className="text-lg font-semibold mb-2">Hints</h3>
                            <ul className="space-y-2">
                                {challenge.hints.map((hint, index) => (
                                    <li key={index} className="text-muted-foreground">
                                        {hint}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>

                {/* Flag Submission Section */}
                {!solved && (
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label htmlFor="flag" className="block text-sm font-medium mb-2">
                                Submit Flag
                            </label>
                            <div className="flex gap-3">
                                <input
                                    type="text"
                                    id="flag"
                                    value={flag}
                                    onChange={(e) => setFlag(e.target.value)}
                                    placeholder="flag{...}"
                                    className={`flex-1 px-4 py-2 bg-background border rounded-lg focus:outline-none font-mono ${
                                        result?.status === "success"
                                            ? "border-green-500 text-green-600 focus:ring-2 focus:ring-green-500"
                                            : "border-border focus:ring-2 focus:ring-primary"
                                    }`}
                                    required
                                />
                                <button
                                    type="submit"
                                    disabled={submitting || !flag}
                                    className="px-6 py-2 bg-primary text-primary-foreground rounded-lg font-semibold hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                                >
                                    <Flag className="w-4 h-4" />
                                    {submitting ? "Submitting..." : "Submit"}
                                </button>
                            </div>
                        </div>

                        {result && (
                            <div
                                className={`p-4 rounded-lg flex items-start gap-3 ${
                                    result.status === "success"
                                        ? "bg-green-500/10 border border-green-500"
                                        : result.status === "warning"
                                            ? "bg-yellow-500/10 border border-yellow-500"
                                            : "bg-destructive/10 border border-destructive"
                                }`}
                            >
                                {result.status === "success" ? (
                                    <CheckCircle2 className="w-5 h-5 text-green-500 flex-shrink-0 mt-0.5" />
                                ) : result.status === "warning" ? (
                                    <Info className="w-5 h-5 text-yellow-500 flex-shrink-0 mt-0.5" />
                                ) : (
                                    <AlertCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                                )}
                                <p
                                    className={`text-sm ${
                                        result.status === "success"
                                            ? "text-green-500"
                                            : result.status === "warning"
                                                ? "text-yellow-500"
                                                : "text-destructive"
                                    }`}
                                >
                                    {result.message}
                                </p>
                            </div>
                        )}
                    </form>
                )}

                {solved && (
                    <div className="p-4 bg-green-500/10 border border-green-500 rounded-lg flex items-center gap-3">
                        <CheckCircle2 className="w-5 h-5 text-green-500" />
                        <p className="text-green-500 font-semibold">
                            {result?.message || "Challenge Solved! 🎉"}
                        </p>
                    </div>
                )}
            </div>

            {/* 🆕 Terminal Modal - NEU */}
            {showTerminal && (
                <KaliTerminal onClose={() => setShowTerminal(false)} />
            )}
        </div>
    );
}