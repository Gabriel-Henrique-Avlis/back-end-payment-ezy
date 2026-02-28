#!/bin/bash
# Script to setup Docker environment for Payment API

set -e

echo "=========================================="
echo "Payment API - Docker Setup"
echo "=========================================="
echo ""

# Check if .env exists
if [ -f .env ]; then
    echo "⚠️  .env file already exists"
    read -p "Do you want to overwrite it? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Exiting without changes."
        exit 0
    fi
fi

# Generate encryption key
echo "Generating encryption key..."
ENCRYPTION_KEY=$(openssl rand -base64 32)

if [ -z "$ENCRYPTION_KEY" ]; then
    echo "❌ Failed to generate encryption key. Is OpenSSL installed?"
    exit 1
fi

# Create .env file
cat > .env << EOF
# Generated on $(date)
ENCRYPTION_KEY=$ENCRYPTION_KEY
EOF

echo "✅ .env file created successfully"
echo ""
echo "Encryption Key: $ENCRYPTION_KEY"
echo ""
echo "Next steps:"
echo "  1. Review the .env file (optional)"
echo "  2. Run: docker-compose up --build"
echo "  3. Wait for services to be healthy"
echo "  4. Test the API: curl http://localhost:8080/payments"
echo ""
echo "For detailed instructions, see DOCKER.md"
