#!/bin/bash
# Appends a session entry to the Obsidian vault's Sessions log.
# Called by Claude Code's Stop hook.

VAULT="/Users/sanyavertolet/IdeaProjects/edukate/edukate-vault"
LOG="$VAULT/Logs/Sessions.md"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M')
DATE=$(date '+%Y-%m-%d')

INPUT=$(cat)
TRANSCRIPT=$(echo "$INPUT" | jq -r '.transcript_summary // "No summary available"' 2>/dev/null)

# Create log file with frontmatter if it doesn't exist or is empty
if [ ! -s "$LOG" ]; then
    cat > "$LOG" << 'HEADER'
---
tags: [log, sessions]
---

# Session Log

HEADER
fi

# Append session entry
cat >> "$LOG" << EOF

## $TIMESTAMP

$TRANSCRIPT

---

EOF
