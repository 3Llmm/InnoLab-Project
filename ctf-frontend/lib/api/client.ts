// For server-side rendering in Docker, use the service name 'app'
// For client-side (browser), use localhost
const getApiBaseUrl = () => {
    // If explicitly set via NEXT_PUBLIC_API_URL, use that (for browser)
    if (process.env.NEXT_PUBLIC_API_URL) {
        // If we're on the server (SSR), use the internal Docker service name
        if (typeof window === 'undefined') {
            return process.env.API_URL || 'http://app:8080';
        }
        // If we're in the browser, use the public URL
        return process.env.NEXT_PUBLIC_API_URL;
    }
    // If running on server (SSR), use Docker service name
    if (typeof window === 'undefined') {
        return process.env.API_URL || 'http://app:8080';
    }
    // If running in browser, use localhost
    return 'http://localhost:8080';
};

// Helper function to extract filename from Content-Disposition header
function extractFilenameFromHeaders(headers: Headers): string | null {
    const contentDisposition = headers.get('Content-Disposition');
    if (!contentDisposition) return null;

    // Try different patterns for filename extraction
    const patterns = [
        /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/,
        /filename\*?=["']?([^"'\n]+)["']?/i,
        /filename\*?=([^;\n]+)/
    ];

    for (const pattern of patterns) {
        const matches = contentDisposition.match(pattern);
        if (matches && matches[1]) {
            // Clean up the filename (remove quotes, handle UTF-8 encoding, etc.)
            let filename = matches[1].replace(/['"]/g, '');

            // Handle RFC 5987 encoded filenames (filename*=UTF-8''...)
            if (filename.startsWith("UTF-8''")) {
                filename = decodeURIComponent(filename.substring(7));
            }

            return filename;
        }
    }

    return null;
}

export class ApiClient {
    private getBaseUrl(): string {
        return getApiBaseUrl();
    }

    // Main request method for JSON responses
    async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
        const url = `${this.getBaseUrl()}${endpoint}`;

        const config: RequestInit = {
            headers: {
                'Content-Type': 'application/json',
                ...options.headers,
            },
            credentials: 'include', // Send cookies with every request
            ...options,
        };

        const response = await fetch(url, config);

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        return response.json();
    }

    // Method for blob/binary responses (file downloads)
    async requestBlob(endpoint: string, options: RequestInit = {}): Promise<{ blob: Blob; filename?: string }> {
        const url = `${this.getBaseUrl()}${endpoint}`;

        const config: RequestInit = {
            headers: {
                'Accept': 'application/octet-stream, */*',
                ...options.headers,
            },
            credentials: 'include',
            ...options,
        };

        const response = await fetch(url, config);

        if (!response.ok) {
            // Try to parse error as JSON first, fall back to text
            let errorMessage = `HTTP error! status: ${response.status}`;
            try {
                const errorText = await response.text();
                // Try to parse as JSON
                try {
                    const errorData = JSON.parse(errorText);
                    errorMessage = errorData.message || errorMessage;
                } catch {
                    // Not JSON, use as text
                    errorMessage = errorText || errorMessage;
                }
            } catch (e) {
                // Couldn't read response body
                errorMessage = `${response.status} ${response.statusText}`;
            }
            throw new Error(errorMessage);
        }

        const blob = await response.blob();
        const filename = extractFilenameFromHeaders(response.headers);

        return { blob, filename };
    }

    // Convenience method for downloading files with automatic trigger
    async downloadFile(endpoint: string, defaultFilename?: string): Promise<void> {
        const { blob, filename } = await this.requestBlob(endpoint);

        // Determine the final filename
        const finalFilename = filename || defaultFilename || 'download';

        // Create and trigger download
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = finalFilename;
        document.body.appendChild(a);
        a.click();

        // Cleanup
        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);
    }

    // Convenience methods
    async post<T>(endpoint: string, data: any): Promise<T> {
        return this.request<T>(endpoint, {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    async get<T>(endpoint: string): Promise<T> {
        return this.request<T>(endpoint, {
            method: 'GET',
        });
    }

    async put<T>(endpoint: string, data: any): Promise<T> {
        return this.request<T>(endpoint, {
            method: 'PUT',
            body: JSON.stringify(data),
        });
    }

    async delete<T>(endpoint: string): Promise<T> {
        return this.request<T>(endpoint, {
            method: 'DELETE',
        });
    }

    // Method to get blob without triggering download (for previews, etc.)
    async getBlob(endpoint: string): Promise<Blob> {
        const { blob } = await this.requestBlob(endpoint);
        return blob;
    }
}

export const apiClient = new ApiClient();