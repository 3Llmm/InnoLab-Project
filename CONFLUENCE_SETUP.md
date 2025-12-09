# Confluence API Setup Guide

This guide explains how to set up Confluence API credentials for the CTF Platform.

## Prerequisites

- Access to the Confluence space
- An Atlassian account with read permissions to the Confluence pages

## Setup Instructions

### Step 1: Generate Your API Token

1. Go to: https://id.atlassian.com/manage-profile/security/api-tokens
2. Click "Create API token"
3. Enter a label (e.g., "CTF Platform")
4. Click "Create"
5. Copy the token immediately - you won't be able to see it again!

### Step 2: Create Your `.env` File

1. In the project root directory, there is already a file named `.env.Confluence`
2. Add the following content:

CONFLUENCE_EMAIL=your-email@example.com
CONFLUENCE_API_TOKEN=your-api-token-here

3. Replace:
   - `your-email@example.com` with your Atlassian account email
   - `your-api-token-here` with the API token you copied

### Step 3: Verify Setup

1. Make sure `.env.Confluence` is in `.gitignore` (it should be by default)
2. Restart Docker containers:
   docker compose down
   docker compose up -d