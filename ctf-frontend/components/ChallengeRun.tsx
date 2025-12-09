import React, { useEffect, useState } from "react";
import TerminalComponent from "../components/Terminal";
import DesktopViewer from "../components/DesktopViewer";

interface ChallengeInstance {
    instanceId: string;
    sshPort: number;
    vscodePort: number;
    desktopPort: number;
    expiresAt: string;
}

interface ChallengeRunProps {
    challengeId: string;
    backendUrl: string;
    domain: string;
}

const ChallengeRun: React.FC<ChallengeRunProps> = ({ challengeId, backendUrl, domain }) => {
    const [instance, setInstance] = useState<ChallengeInstance | null>(null);

    useEffect(() => {
        fetch(`${backendUrl}/environment/start/${challengeId}`, {
            method: "POST",
            credentials: "include"
        })
            .then((res) => res.json())
            .then((data) => setInstance(data))
            .catch((err) => console.error("Failed to start environment", err));
    }, [backendUrl, challengeId]);

    if (!instance) return <div>Loading environment...</div>;

    return (
        <div style={{ padding: "20px" }}>
            <h1>Challenge Environment</h1>

            {/* SSH section */}
            <div style={{ marginBottom: "20px" }}>
                <h3>SSH Access</h3>
                <pre style={{ backgroundColor: "#222", padding: "10px", borderRadius: "4px" }}>
ssh ctfuser@{domain} -p {instance.sshPort}
        </pre>
            </div>

            {/* Terminal */}
            <div style={{ marginBottom: "40px" }}>
                <h3>Web Terminal</h3>
                <TerminalComponent
                    instanceId={instance.instanceId}
                    backendUrl={backendUrl.replace("http", "ws")}
                />
            </div>

            {/* Desktop */}
            <div>
                <h3>Desktop Environment (noVNC)</h3>
                <DesktopViewer
                    desktopPort={instance.desktopPort}
                    domain={domain}
                />
            </div>
        </div>
    );
};

export default ChallengeRun;
