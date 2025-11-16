import React from "react";

interface DesktopViewerProps {
    desktopPort: number;
    domain: string;
}

const DesktopViewer: React.FC<DesktopViewerProps> = ({ desktopPort, domain }) => {
    const url = `http://${domain}:${desktopPort}/vnc.html?autoconnect=true&password=ctf`;

    return (
        <iframe
            src={url}
            style={{
                width: "100%",
                height: "600px",
                border: "none",
                borderRadius: "8px",
            }}
            title="Desktop Viewer"
        />
    );
};

export default DesktopViewer;
