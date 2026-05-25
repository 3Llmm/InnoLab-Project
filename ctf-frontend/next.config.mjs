const nextConfig = {
  output: 'standalone',
  trailingSlash: true,
  reactStrictMode: true,
  typescript: { ignoreBuildErrors: true },
  images: { unoptimized: true },
  turbopack: {
    root: '.'
  },
}
export default nextConfig