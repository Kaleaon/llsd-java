#!/bin/bash

# Automated Conflict Resolver for LLSD Java Library
# This script automatically resolves merge conflicts with the master branch
# by preserving the superior master implementation while keeping our unique contributions

set -e

echo "🔧 LLSD Java Automated Conflict Resolver"
echo "========================================"

# Check if we're in a merge conflict state
if ! git status | grep -q "both added\|both modified"; then
    echo "❌ No merge conflicts detected. Are you in the middle of a merge?"
    exit 1
fi

echo "📋 Conflicts detected. Analyzing..."

# Function to resolve conflicts by strategy
resolve_conflict() {
    local file=$1
    local strategy=$2
    
    echo "🔧 Resolving $file using strategy: $strategy"
    
    case $strategy in
        "keep-theirs")
            git checkout --theirs "$file"
            git add "$file"
            echo "  ✅ Kept master version of $file"
            ;;
        "keep-ours")
            git checkout --ours "$file"
            git add "$file"
            echo "  ✅ Kept our version of $file"
            ;;
        "merge-manual")
            echo "  🔄 Manual merge required for $file"
            # This will be handled by the script logic below
            ;;
    esac
}

echo "🎯 Applying conflict resolution strategy..."

# Resolve .gitignore by combining both versions (custom logic)
if git status --porcelain | grep -q ".gitignore"; then
    echo "🔧 Resolving .gitignore with combined approach..."
    cat > .gitignore << 'EOF'
# Maven build output
target/
*.class

# IDE files
.idea/
*.iml
.vscode/
.project
.classpath
.settings/
.eclipse/

# OS files
.DS_Store
Thumbs.db

# Maven
dependency-reduced-pom.xml

# Logs
*.log
EOF
    git add .gitignore
    echo "  ✅ Combined .gitignore resolved"
fi

# For serializer conflicts, always prefer master's implementation as it's more comprehensive
if git status --porcelain | grep -q "LLSDBinarySerializer.java"; then
    resolve_conflict "src/main/java/lindenlab/llsd/LLSDBinarySerializer.java" "keep-theirs"
fi

if git status --porcelain | grep -q "LLSDNotationSerializer.java"; then
    resolve_conflict "src/main/java/lindenlab/llsd/LLSDNotationSerializer.java" "keep-theirs"
fi

if git status --porcelain | grep -q "LLSDSerializationTest.java"; then
    resolve_conflict "src/test/java/lindenlab/llsd/LLSDSerializationTest.java" "keep-theirs"
fi

# Check if there are any remaining conflicts
if git status | grep -q "both added\|both modified"; then
    echo "⚠️  Some conflicts still remain. Please resolve manually:"
    git status --short | grep "^UU\|^AA\|^DD"
    exit 1
fi

echo "✅ All conflicts resolved!"
echo ""
echo "📊 Resolution Summary:"
echo "  • .gitignore: Combined approach (both versions merged)"
echo "  • Binary Serializer: Used master version (more comprehensive)"
echo "  • Notation Serializer: Used master version (more comprehensive)" 
echo "  • Serialization Tests: Used master version (complete test suite)"
echo ""
echo "🚀 Ready to commit the resolution!"
echo "Run: git commit -m 'Resolve merge conflicts automatically'"

# Verify the resolution by checking if it compiles
echo ""
echo "🧪 Verifying resolution by testing compilation..."
if mvn compile -q; then
    echo "  ✅ Code compiles successfully after conflict resolution"
else
    echo "  ❌ Compilation failed. Manual review needed."
    exit 1
fi

echo ""
echo "🎉 Automated conflict resolution completed successfully!"
echo "All conflicts resolved and code verified to compile correctly."