"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import {
    ArrowLeft,
    Flag,
    Download,
    AlertCircle,
    CheckCircle2,
    Info,
    Terminal,
    XCircle,
    Play,
    Square,
    ExternalLink,
    Clock,
    Wifi,
    Monitor,
    Code,
} from "lucide-react";
import { apiClient } from "@/lib/api/client";
import KaliTerminal from "@/components/KaliTerminalClient";
import type { Challenge, EnvironmentInstance } from "@/lib/types";
import { hasDownloadableFiles, requiresInstance } from "@/lib/types";

interface ChallengeDetailProps {
    challenge: Challenge;
}


export default function ChallengeDetail({ challenge }: ChallengeDetailProps) {
    const [flag, setFlag] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [downloading, setDownloading] = useState(false);
    const [showTerminal, setShowTerminal] = useState(false);
    const [startingTerminal, setStartingTerminal] = useState(false);
    const [stoppingTerminal, setStoppingTerminal] = useState(false);
    const [result, setResult] = useState<{
        status: "success" | "error" | "warning" | "info";
        message: string;
    } | null>(null);
    const [solved, setSolved] = useState<boolean>(!!challenge.solved);
    const [environment, setEnvironment] = useState<EnvironmentInstance | null>(null);
    const [timeRemaining, setTimeRemaining] = useState<string>("");

    // Check for existing environment on component mount
    useEffect(() => {
        const checkExistingEnvironment = async () => {
            try {
                // If challenge has an instance ID stored, check its status
                if (challenge.instanceId) {
                    const res = await apiClient.get<EnvironmentInstance>(
                        `/api/environment/instance/${challenge.instanceId}`
                    );
                    if (res.status === "RUNNING") {
                        setEnvironment(res);
                        setShowTerminal(true);
                    }
                }
            } catch (error) {
                console.error("Error checking existing environment:", error);
            }
        };

        checkExistingEnvironment();
    }, [challenge.instanceId]);

    // Update time remaining every minute
    useEffect(() => {
        if (!environment) {
            setTimeRemaining("");
            return;
        }

        const updateTimeRemaining = () => {
            const expires = new Date(environment.expiresAt);
            const now = new Date();
            const diffMs = expires.getTime() - now.getTime();

            if (diffMs <= 0) {
                setTimeRemaining("Expired");
                // Auto-cleanup expired environment
                if (environment.status === "RUNNING") {
                    handleStopTerminal();
                }
                return;
            }

            const diffMins = Math.floor(diffMs / 60000);
            const diffSecs = Math.floor((diffMs % 60000) / 1000);

            if (diffMins > 0) {
                setTimeRemaining(`${diffMins}m ${diffSecs}s remaining`);
            } else {
                setTimeRemaining(`${diffSecs}s remaining`);
            }
        };

        updateTimeRemaining();
        const intervalId = setInterval(updateTimeRemaining, 1000);

        return () => clearInterval(intervalId);
    }, [environment]);

    // Auto-poll environment status every 30 seconds when terminal is open
    useEffect(() => {
        let intervalId: NodeJS.Timeout;

        if (environment && showTerminal && environment.status === "RUNNING") {
            intervalId = setInterval(async () => {
                try {
                    const res = await apiClient.get<EnvironmentInstance>(
                        `/api/environment/instance/${environment.instanceId}`
                    );
                    setEnvironment(res);

                    // If environment stopped or expired, close terminal
                    if (res.status !== "RUNNING") {
                        setShowTerminal(false);
                        setResult({
                            status: "info",
                            message: "Environment has been stopped or expired."
                        });
                    }
                } catch (error) {
                    console.error("Error polling environment:", error);
                }
            }, 30000);
        }

        return () => {
            if (intervalId) clearInterval(intervalId);
        };
    }, [environment, showTerminal]);

    async function handleLaunchTerminal() {
        if (environment) {
            setShowTerminal(true);
            return;
        }

        setStartingTerminal(true);
        setResult(null);

        try {
            console.log("🚀 Building and starting environment for challenge:", challenge.id);

            // ALWAYS use the build endpoint for now
            const res = await apiClient.post<EnvironmentInstance>(
                `/api/environment/build/${challenge.id}`
            );

            console.log("✅ Environment built and started:", res);
            setEnvironment(res);
            setShowTerminal(true);

            setResult({
                status: "success",
                message: `Environment built and started successfully! SSH Port: ${res.sshPort}`
            });
        } catch (error: any) {
            console.error("❌ Failed to build and start environment:", error);
            setResult({
                status: "error",
                message: `Failed to start environment: ${error?.message || "Unknown error"}`,
            });
        } finally {
            setStartingTerminal(false);
        }
    }

    async function handleStopTerminal() {
        if (!environment?.instanceId) return;

        setStoppingTerminal(true);

        try {
            await apiClient.post(`/api/environment/stop/${environment.instanceId}`);
            setEnvironment(null);
            setShowTerminal(false);
            setResult({
                status: "success",
                message: "Environment stopped successfully"
            });
        } catch (error: any) {
            console.error("❌ Failed to stop environment:", error);
            setResult({
                status: "error",
                message: `Failed to stop environment: ${error?.message || "Unknown error"}`,
            });
        } finally {
            setStoppingTerminal(false);
        }
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!flag.trim()) return;

        setSubmitting(true);
        setResult(null);

        try {
            const response = await apiClient.post<{
                status: string;
                message: string;
            }>("/api/flags/submit", {
                challengeId: challenge.id,
                flag: flag.trim()
            });

            setResult({
                status: response?.status as any || "error",
                message: response?.message || "",
            });

            if (response?.status === "success") {
                setFlag("");
                setSolved(true);
            }
        } catch (error: any) {
            setResult({
                status: "error",
                message: error?.message || "An error occurred while submitting flag",
            });
        } finally {
            setSubmitting(false);
        }
    };

    const handleDownload = async () => {
        if (!challenge.id) return;

        // Check if challenge has downloadable files
        const fileUrl = challenge.fileurl || challenge.downloadUrl;
        if (!fileUrl) {
            setResult({
                status: "warning",
                message: "This challenge doesn't have any downloadable files"
            });
            return;
        }

        setDownloading(true);
        setResult(null); // Clear any previous results

        try {
            // Use the API client for consistent authentication and error handling
            // The endpoint is /api/challenges/{id}/download, not the full URL
            await apiClient.downloadFile(
                `/api/challenges/${challenge.id}/download`,
                challenge.originalFilename || `${challenge.id}.zip`
            );

            // Success message
            setResult({
                status: "success",
                message: "Challenge files downloaded successfully!"
            });

        } catch (error: any) {
            console.error("Download error:", error);

            // User-friendly error messages based on error type
            let userMessage = error.message;
            const errorLower = error.message.toLowerCase();

            if (errorLower.includes("401") || errorLower.includes("unauthorized")) {
                userMessage = "Please log in to download challenge files.";
            } else if (errorLower.includes("403") || errorLower.includes("forbidden")) {
                userMessage = "You don't have permission to download these files.";
            } else if (errorLower.includes("404") || errorLower.includes("not found")) {
                userMessage = "Files not found for this challenge.";
            } else if (errorLower.includes("500") || errorLower.includes("server error")) {
                userMessage = "Server error. Please try again later.";
            } else if (errorLower.includes("network") || errorLower.includes("failed to fetch")) {
                userMessage = "Network error. Please check your connection.";
            } else if (errorLower.includes("timeout")) {
                userMessage = "Download timed out. Please try again.";
            }

            setResult({
                status: "error",
                message: userMessage,
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

    const copyToClipboard = (text: string, label: string) => {
        navigator.clipboard.writeText(text).then(() => {
            setResult({
                status: "success",
                message: `${label} copied to clipboard!`
            });
        });
    };

    const hasDownload = hasDownloadableFiles(challenge);
    const showEnvironmentSection = requiresInstance(challenge);

    return (
        <div className="space-y-6">
            <Link
                href="/challenges"
                className="inline-flex items-center gap-2 text-muted-foreground hover:text-primary transition-colors"
            >
                <ArrowLeft className="w-4 h-4" />
                Back to Challenges
            </Link>

            <div className="bg-card p-6 md:p-8 rounded-lg border border-border shadow-sm">
                <div className="flex flex-col lg:flex-row lg:items-start justify-between gap-6 mb-8">
                    <div className="flex-1">
                        <h1 className="text-2xl md:text-3xl font-bold mb-3">{challenge.title}</h1>
                        <div className="flex flex-wrap gap-3 mb-4">
                            <span
                                className={`text-sm font-semibold px-3 py-1 rounded-full ${
                                    difficultyColors[challenge.difficulty] || difficultyColors.easy
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
                            {solved && (
                                <span className="text-sm px-3 py-1 bg-green-500/10 text-green-500 rounded-full">
                                    SOLVED ✓
                                </span>
                            )}
                        </div>
                        <p className="text-muted-foreground">{challenge.description}</p>
                    </div>
                    <div className="lg:text-right">
                        <div className="text-3xl font-bold text-accent">
                            {challenge.points}
                        </div>
                        <div className="text-sm text-muted-foreground">points</div>
                    </div>
                </div>

                {/* Results Display */}
                {result && (
                    <div
                        className={`mb-6 p-4 rounded-lg flex items-start gap-3 ${
                            result.status === "success"
                                ? "bg-green-500/10 border border-green-500"
                                : result.status === "warning"
                                    ? "bg-yellow-500/10 border border-yellow-500"
                                    : result.status === "info"
                                        ? "bg-blue-500/10 border border-blue-500"
                                        : "bg-destructive/10 border border-destructive"
                        }`}
                    >
                        {result.status === "success" ? (
                            <CheckCircle2 className="w-5 h-5 text-green-500 flex-shrink-0 mt-0.5" />
                        ) : result.status === "warning" ? (
                            <Info className="w-5 h-5 text-yellow-500 flex-shrink-0 mt-0.5" />
                        ) : result.status === "info" ? (
                            <Info className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                        ) : (
                            <AlertCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
                        )}
                        <p
                            className={`text-sm ${
                                result.status === "success"
                                    ? "text-green-500"
                                    : result.status === "warning"
                                        ? "text-yellow-500"
                                        : result.status === "info"
                                            ? "text-blue-500"
                                            : "text-destructive"
                            }`}
                        >
                            {result.message}
                        </p>
                    </div>
                )}

                {/* Download Section - Only show if challenge has files */}
                {hasDownload && (
                    <div className="mb-8">
                        <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                            <Download className="w-5 h-5" />
                            Challenge Files
                        </h2>
                        <button
                            onClick={handleDownload}
                            disabled={downloading}
                            className="flex items-center gap-2 px-4 py-2.5 bg-primary text-primary-foreground rounded-lg hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <Download className="w-4 h-4" />
                            {downloading ? "Downloading..." : "Download Challenge File"}
                        </button>
                        <p className="text-sm text-muted-foreground mt-2">
                            Download the challenge files to get started
                        </p>
                    </div>
                )}

                {/* Terminal Environment Section - Only show if challenge requires instance */}
                {showEnvironmentSection && (
                    <div className="mb-8">
                        <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                            <Terminal className="w-5 h-5" />
                            Interactive Environment
                        </h2>

                        <div className="space-y-4">
                            {/* Control Buttons */}
                            <div className="flex flex-wrap gap-3">
                                <button
                                    onClick={handleLaunchTerminal}
                                    disabled={startingTerminal || (environment?.status === "RUNNING")}
                                    className={`flex items-center gap-2 px-4 py-2.5 rounded-lg font-medium transition-colors ${
                                        environment?.status === "RUNNING"
                                            ? "bg-green-600 text-white cursor-default"
                                            : "bg-green-600 hover:bg-green-700 text-white"
                                    } disabled:opacity-50 disabled:cursor-not-allowed`}
                                >
                                    {startingTerminal ? (
                                        <>
                                            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                            Starting...
                                        </>
                                    ) : environment?.status === "RUNNING" ? (
                                        <>
                                            <CheckCircle2 className="w-4 h-4" />
                                            Environment Running
                                        </>
                                    ) : (
                                        <>
                                            <Play className="w-4 h-4" />
                                            Start Environment
                                        </>
                                    )}
                                </button>

                                {environment?.status === "RUNNING" && (
                                    <>
                                        <button
                                            onClick={() => setShowTerminal(!showTerminal)}
                                            className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
                                        >
                                            {showTerminal ? (
                                                <>
                                                    <XCircle className="w-4 h-4" />
                                                    Hide Terminal
                                                </>
                                            ) : (
                                                <>
                                                    <Terminal className="w-4 h-4" />
                                                    Open Terminal
                                                </>
                                            )}
                                        </button>

                                        <button
                                            onClick={handleStopTerminal}
                                            disabled={stoppingTerminal}
                                            className="flex items-center gap-2 px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                                        >
                                            {stoppingTerminal ? (
                                                <>
                                                    <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                                    Stopping...
                                                </>
                                            ) : (
                                                <>
                                                    <Square className="w-4 h-4" />
                                                    Stop Environment
                                                </>
                                            )}
                                        </button>
                                    </>
                                )}
                            </div>

                            {/* Environment Details */}
                            {environment && (
                                <div className="bg-background border border-border rounded-lg p-4 space-y-3">
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-2">
                                            <Clock className="w-4 h-4 text-muted-foreground" />
                                            <span className="text-sm font-medium">Status:</span>
                                            <span className={`text-sm px-2 py-1 rounded ${
                                                environment.status === "RUNNING"
                                                    ? "bg-green-500/10 text-green-500"
                                                    : "bg-yellow-500/10 text-yellow-500"
                                            }`}>
                                                {environment.status}
                                            </span>
                                        </div>
                                        <div className="flex items-center gap-2">
                                            <Clock className="w-4 h-4 text-muted-foreground" />
                                            <span className="text-sm text-muted-foreground">{timeRemaining}</span>
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
                                        {/* SSH Access */}
                                        <div className="border rounded-lg p-3">
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center gap-2">
                                                    <Wifi className="w-4 h-4 text-muted-foreground" />
                                                    <span className="text-sm font-medium">SSH Access</span>
                                                </div>
                                                <button
                                                    onClick={() => copyToClipboard(`ssh user@localhost -p ${environment.sshPort}`, "SSH Command")}
                                                    className="text-xs text-primary hover:underline"
                                                >
                                                    Copy
                                                </button>
                                            </div>
                                            <code className="text-xs bg-muted p-2 rounded block font-mono">
                                                ssh user@localhost -p {environment.sshPort}
                                            </code>
                                        </div>

                                        {/* VS Code Access */}
                                        <div className="border rounded-lg p-3">
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center gap-2">
                                                    <Code className="w-4 h-4 text-muted-foreground" />
                                                    <span className="text-sm font-medium">VS Code</span>
                                                </div>
                                                <a
                                                    href={`http://localhost:${environment.vscodePort}`}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="text-xs text-primary hover:underline flex items-center gap-1"
                                                >
                                                    Open <ExternalLink className="w-3 h-3" />
                                                </a>
                                            </div>
                                            <code className="text-xs bg-muted p-2 rounded block font-mono">
                                                http://localhost:{environment.vscodePort}
                                            </code>
                                        </div>

                                        {/* Desktop Access */}
                                        <div className="border rounded-lg p-3">
                                            <div className="flex items-center justify-between mb-2">
                                                <div className="flex items-center gap-2">
                                                    <Monitor className="w-4 h-4 text-muted-foreground" />
                                                    <span className="text-sm font-medium">Desktop GUI</span>
                                                </div>
                                                <a
                                                    href={`http://localhost:${environment.desktopPort}`}
                                                    target="_blank"
                                                    rel="noopener noreferrer"
                                                    className="text-xs text-primary hover:underline flex items-center gap-1"
                                                >
                                                    Open <ExternalLink className="w-3 h-3" />
                                                </a>
                                            </div>
                                            <code className="text-xs bg-muted p-2 rounded block font-mono">
                                                http://localhost:{environment.desktopPort}
                                            </code>
                                        </div>
                                    </div>

                                    <div className="text-xs text-muted-foreground pt-2 border-t">
                                        Instance ID: {environment.instanceId}
                                    </div>
                                </div>
                            )}

                            {!environment && (
                                <p className="text-sm text-muted-foreground">
                                    Start an environment to get SSH access, a web-based VS Code editor, and a desktop GUI with Kali Linux tools.
                                </p>
                            )}
                        </div>
                    </div>
                )}

                {/* Info message if no download or environment */}
                {!hasDownload && !showEnvironmentSection && (
                    <div className="mb-8 p-4 bg-blue-500/10 border border-blue-500 rounded-lg">
                        <div className="flex items-start gap-3">
                            <Info className="w-5 h-5 text-blue-500 flex-shrink-0 mt-0.5" />
                            <div>
                                <h3 className="font-medium text-blue-500 mb-1">No Additional Files or Environment Needed</h3>
                                <p className="text-sm text-blue-600/80">
                                    This challenge doesn't require any downloadable files or a separate environment.
                                    Use your existing tools and knowledge to analyze and solve the challenge.
                                </p>
                            </div>
                        </div>
                    </div>
                )}

                {/* Flag Submission Section */}
                {!solved && (
                    <div>
                        <h2 className="text-lg font-semibold mb-4 flex items-center gap-2">
                            <Flag className="w-5 h-5" />
                            Submit Flag
                        </h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
                            <div>
                                <div className="flex flex-col sm:flex-row gap-3">
                                    <input
                                        type="text"
                                        id="flag"
                                        value={flag}
                                        onChange={(e) => setFlag(e.target.value)}
                                        placeholder="flag{...}"
                                        className={`flex-1 px-4 py-2.5 bg-background border rounded-lg focus:outline-none font-mono placeholder:text-muted-foreground ${
                                            result?.status === "success"
                                                ? "border-green-500 text-green-600 focus:ring-2 focus:ring-green-500"
                                                : "border-border focus:ring-2 focus:ring-primary"
                                        }`}
                                        required
                                        disabled={submitting}
                                    />
                                    <button
                                        type="submit"
                                        disabled={submitting || !flag.trim()}
                                        className="sm:w-auto px-6 py-2.5 bg-primary text-primary-foreground rounded-lg font-semibold hover:opacity-90 transition-opacity disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                                    >
                                        {submitting ? (
                                            <>
                                                <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                                                Submitting...
                                            </>
                                        ) : (
                                            <>
                                                <Flag className="w-4 h-4" />
                                                Submit Flag
                                            </>
                                        )}
                                    </button>
                                </div>
                                <p className="text-xs text-muted-foreground mt-2">
                                    Enter the flag you found in the format: flag{`{`}your_flag_here{`}`}
                                </p>
                            </div>
                        </form>
                    </div>
                )}

                {solved && (
                    <div className="p-4 bg-green-500/10 border border-green-500 rounded-lg flex items-center gap-3">
                        <CheckCircle2 className="w-5 h-5 text-green-500" />
                        <div>
                            <p className="text-green-500 font-semibold">Challenge Solved! 🎉</p>
                            <p className="text-sm text-green-600/80 mt-1">
                                You earned {challenge.points} points for solving this challenge.
                            </p>
                        </div>
                    </div>
                )}
            </div>

            {/* KaliTerminal Modal */}
            {showTerminal && environment && environment.status === "RUNNING" && (
                <KaliTerminal
                    instanceId={environment.instanceId}
                    sshPort={environment.sshPort}
                    containerName={environment.containerName || ""}
                    onClose={() => setShowTerminal(false)}
                />
            )}
        </div>
    );
}