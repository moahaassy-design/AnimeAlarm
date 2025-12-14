#!/bin/bash

# Konfigurasi
REPO="moahaassy-design/AnimeAlarm"
INTERVAL=10 # Refresh rate dalam detik

# Kode Warna
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
NC='\033[0m' # No Color

while true; do
    clear
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}   🤖 ANIME ALARM BUILD MONITOR       ${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo "Time: $(date '+%H:%M:%S')"
    echo "Repo: $REPO"
    echo ""
    echo -e "${YELLOW}Fetching latest status...${NC}"
    echo ""

    # Ambil data dari GitHub CLI
    # Kita menggunakan --json untuk parsing status (opsional) atau raw output untuk tampilan
    gh run list --repo "$REPO" --limit 5
    
    echo ""
    echo -e "${BLUE}========================================${NC}"
    
    # Cek status build terakhir untuk notifikasi sederhana
    LATEST_STATUS=$(gh run list --repo "$REPO" --limit 1 --json conclusion -q ".[0].conclusion")
    
    if [ "$LATEST_STATUS" == "success" ]; then
        echo -e "${GREEN}✅ SUCCESS! Build terakhir berhasil.${NC}"
    elif [ "$LATEST_STATUS" == "failure" ]; then
        echo -e "${RED}❌ FAILED! Build terakhir gagal. Cek logs.${NC}"
    elif [ "$LATEST_STATUS" == "cancelled" ]; then
        echo -e "${RED}🚫 CANCELLED.${NC}"
    else
        echo -e "${YELLOW}⏳ Build sedang berjalan...${NC}"
    fi

    echo ""
    echo "Tekan [CTRL+C] untuk berhenti."
    
    sleep $INTERVAL
done
