const nextConfig = {
  output: 'standalone',
  trailingSlash: true,
  reactStrictMode: true,
  eslint: { ignoreDuringBuilds: true },
  typescript: { ignoreBuildErrors: true },
  images: { unoptimized: true },
  turbopack: {
    root: '.'
  },
}
export default nextConfig