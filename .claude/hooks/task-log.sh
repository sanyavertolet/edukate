#!/bin/bash
# Appends completed task entries to the Obsidian vault's Tasks log.
# Called by Claude Code's TaskCompleted hook.

VAULT="/Users/sanyavertolet/IdeaProjects/edukate/edukate-vault"
LOG="$VAULT/Logs/Tasks.md"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M')

INPUT=$(cat)
TASK_NAME=$(echo "$INPUT" | jq -r '.task_name // .task_description // "Unnamed task"' 2>/dev/null)

# Create log file with frontmatter if it doesn't exist or is empty
if [ ! -s "$LOG" ]; then
    cat > "$LOG" << 'HEADER'
---
tags: [log, tasks]
---

# Completed Tasks

HEADER
fi

# Append task entry
echo "- [x] **$TIMESTAMP** — $TASK_NAME" >> "$LOG"
