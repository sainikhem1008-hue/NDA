#!/bin/bash

echo "🚀 SETTING UP GITHUB WORKFLOW FOR ANDROID APK"
echo "============================================="
echo ""

echo "📋 STEP 1: Create GitHub Repository"
echo "   1. Go to https://github.com"
echo "   2. Click 'New repository' (green button)"
echo "   3. Repository name: night-duty-calculator"
echo "   4. Make it Public"
echo "   5. Click 'Create repository'"
echo ""

echo "📋 STEP 2: Copy your repository URL"
echo "   It will look like: https://github.com/YOUR_USERNAME/night-duty-calculator.git"
echo ""

read -p "🔗 Enter your GitHub repository URL: " repo_url

if [ -z "$repo_url" ]; then
    echo "❌ No URL provided. Please run the script again with your repository URL."
    exit 1
fi

echo ""
echo "🔧 Adding remote repository..."
git remote add origin "$repo_url"

if [ $? -eq 0 ]; then
    echo "✅ Remote added successfully!"
    echo ""
    echo "🚀 Pushing to GitHub (this will trigger the workflow)..."
    git push -u origin main
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "🎉 SUCCESS! GitHub workflow triggered!"
        echo ""
        echo "📱 Your APK will be ready in 3-5 minutes!"
        echo ""
        echo "📋 How to get your APK:"
        echo "   1. Go to your GitHub repository: $repo_url"
        echo "   2. Click 'Actions' tab"
        echo "   3. Click on 'Build Android APK' workflow"
        echo "   4. Wait for green checkmark (✓)"
        echo "   5. Scroll down to 'Artifacts' section"
        echo "   6. Click 'night-duty-calculator-apk' to download"
        echo ""
        echo "🎯 Features included in your APK:"
        echo "   ✅ Button always enabled for allowance calculation"
        echo "   ✅ Shows 'you are on leave/rest' for leave/rest days"
        echo "   ✅ No duty hours added to PDF totals for leave/rest days"
        echo "   ✅ PDF export crash fixed"
        echo "   ✅ Complete leave management system"
        echo ""
        echo "📱 APK will be ~12.9 MB and ready to install!"
    else
        echo ""
        echo "❌ Failed to push to GitHub"
        echo "Please check your repository URL and try again"
    fi
else
    echo ""
    echo "❌ Failed to add remote"
    echo "Please check your repository URL and try again"
fi
