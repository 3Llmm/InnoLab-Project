"use client";

import dynamic from 'next/dynamic';
import { ComponentType } from 'react';

interface KaliTerminalProps {
    instanceId?: string;
    sshPort?: number;
    onClose: () => void;
}

// Dynamically import with no SSR
const KaliTerminal = dynamic<KaliTerminalProps>(
  () => import('./KaliTerminal'),
  { 
    ssr: false,
    loading: () => (
      <div className="fixed inset-0 bg-black/80 flex items-center justify-center">
        <div className="text-white">Loading terminal...</div>
      </div>
    )
  }
);

export default KaliTerminal;