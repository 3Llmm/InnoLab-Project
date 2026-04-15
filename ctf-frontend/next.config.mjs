const nextConfig = {
  output: 'standalone',  // Keep this from your branch
  trailingSlash: true,
  reactStrictMode: true,
  eslint: { ignoreDuringBuilds: true },
  typescript: { ignoreBuildErrors: true },
  images: { unoptimized: true },
  async rewrites() {
    return [
      { source: '/api/:path*', destination: `${process.env.API_PROXY_TARGET || 'http://app:8080'}/api/:path*` },
    ]
  },
}
export default nextConfig