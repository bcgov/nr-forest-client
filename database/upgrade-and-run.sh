#!/bin/bash
set -e

# If running as root, ensure postgres ownership and re-execute as postgres user
if [ "$(id -u)" = '0' ]; then
    chown -R postgres:postgres /var/lib/postgresql 2>/dev/null || true
    exec gosu postgres "$BASH_SOURCE" "$@"
fi

NEW_PGDATA="${PGDATA:-/var/lib/postgresql/18/docker}"
SUPERUSER="${POSTGRES_USER:-postgres}"

OLD_CLUSTER_DIR=""
OLD_VERSION=""

# Check if PG_VERSION exists directly in /var/lib/postgresql
if [ -s "/var/lib/postgresql/PG_VERSION" ]; then
    V=$(cat "/var/lib/postgresql/PG_VERSION")
    if [ "$V" != "18" ]; then
        echo "Detected PostgreSQL $V directly in /var/lib/postgresql."
        # Relocate all files into a subfolder except PG_VERSION first, so an interrupted
        # relocation can safely resume through this root-directory branch on next startup
        OLD_DIR="/var/lib/postgresql/data.v${V}"
        mkdir -p "$OLD_DIR"
        find /var/lib/postgresql -maxdepth 1 -mindepth 1 \
            ! -name "data.v${V}" \
            ! -name "18" \
            ! -name "lost+found" \
            ! -name "PG_VERSION" \
            -exec mv -t "$OLD_DIR" {} +
        # Move PG_VERSION last only after all other entries have moved successfully
        mv /var/lib/postgresql/PG_VERSION "$OLD_DIR/PG_VERSION"
        OLD_CLUSTER_DIR="$OLD_DIR"
        OLD_VERSION="$V"
    fi
# Check if PG_VERSION exists in /var/lib/postgresql/data (default in PostgreSQL 13 container)
elif [ -s "/var/lib/postgresql/data/PG_VERSION" ]; then
    V=$(cat "/var/lib/postgresql/data/PG_VERSION")
    if [ "$V" != "18" ]; then
        echo "Detected PostgreSQL $V in /var/lib/postgresql/data."
        OLD_CLUSTER_DIR="/var/lib/postgresql/data"
        OLD_VERSION="$V"
    fi
# Check if an earlier migration relocated old cluster to /var/lib/postgresql/data.v* (ignoring .bak)
else
    for d in /var/lib/postgresql/data.v*; do
        if [ -d "$d" ] && [[ "$d" != *.bak* ]] && [ -s "$d/PG_VERSION" ]; then
            V=$(cat "$d/PG_VERSION")
            if [ "$V" != "18" ]; then
                echo "Detected PostgreSQL $V in $d."
                OLD_CLUSTER_DIR="$d"
                OLD_VERSION="$V"
                break
            fi
        fi
    done
fi

if [ -n "$OLD_CLUSTER_DIR" ] && [ -n "$OLD_VERSION" ]; then
    echo "=========================================================="
    echo "Initiating automated upgrade from PostgreSQL $OLD_VERSION to 18..."
    echo "Source: $OLD_CLUSTER_DIR"
    echo "Target: $NEW_PGDATA"
    echo "Superuser: $SUPERUSER"
    echo "=========================================================="

    OLD_BIN="/usr/lib/postgresql/${OLD_VERSION}/bin"
    NEW_BIN="/usr/lib/postgresql/18/bin"

    if [ ! -x "$OLD_BIN/postgres" ]; then
        echo "ERROR: PostgreSQL $OLD_VERSION binaries not found at $OLD_BIN!" >&2
        exit 1
    fi

    # Determine checksum settings of old cluster
    CHECKSUM_VAL=$("$OLD_BIN/pg_controldata" "$OLD_CLUSTER_DIR" | grep "Data page checksum version" | awk '{print $NF}')
    INITDB_CHECKSUM_ARG="--no-data-checksums"
    if [ "$CHECKSUM_VAL" = "1" ]; then
        INITDB_CHECKSUM_ARG="--data-checksums"
    fi
    echo "Preserving checksum setting: $INITDB_CHECKSUM_ARG"

    mkdir -p "$NEW_PGDATA"
    rm -rf "$NEW_PGDATA"/*
    chmod 700 "$NEW_PGDATA"
    chmod 700 "$OLD_CLUSTER_DIR"

    # Initialize PostgreSQL 18 cluster with matching superuser and checksums
    "$NEW_BIN/initdb" -D "$NEW_PGDATA" $INITDB_CHECKSUM_ARG -U "$SUPERUSER"

    # Run pg_upgrade
    echo "Executing pg_upgrade..."
    cd /tmp
    "$NEW_BIN/pg_upgrade" \
        -d "$OLD_CLUSTER_DIR" \
        -D "$NEW_PGDATA" \
        -b "$OLD_BIN" \
        -B "$NEW_BIN" \
        -U "$SUPERUSER"

    echo "pg_upgrade completed successfully!"

    # Restore authentication configuration from old cluster so external pod connections are accepted
    if [ -f "$OLD_CLUSTER_DIR/pg_hba.conf" ]; then
        echo "Restoring authentication configuration (pg_hba.conf, pg_ident.conf) from old cluster..."
        cp -p "$OLD_CLUSTER_DIR/pg_hba.conf" "$NEW_PGDATA/pg_hba.conf"
        [ -f "$OLD_CLUSTER_DIR/pg_ident.conf" ] && cp -p "$OLD_CLUSTER_DIR/pg_ident.conf" "$NEW_PGDATA/pg_ident.conf"
    fi

    # Preserve old data cluster as backup so NO DATA IS EVER DELETED
    BACKUP_PATH="/var/lib/postgresql/data.v${OLD_VERSION}.bak"
    if [ "$OLD_CLUSTER_DIR" != "$BACKUP_PATH" ]; then
        echo "Safely retaining old cluster at $BACKUP_PATH..."
        mv "$OLD_CLUSTER_DIR" "$BACKUP_PATH"
    fi
    echo "Upgrade to PostgreSQL 18 complete. Proceeding to startup."
    echo "=========================================================="
fi

# Hand off execution to official docker-entrypoint
exec docker-entrypoint.sh postgres "$@"
