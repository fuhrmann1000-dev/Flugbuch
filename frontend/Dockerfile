# =============================================================
#  Build Stage – Node.js + Angular CLI
# =============================================================
FROM node:22-alpine AS builder

WORKDIR /build

# package.json zuerst → node_modules-Layer wird gecached
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

# Quellcode kopieren und produktiven Build erstellen
COPY . .
RUN npm run build -- --configuration production

# =============================================================
#  Runtime Stage – nginx liefert den statischen Build aus
# =============================================================
FROM nginx:1.27-alpine AS runtime

# Eigene nginx-Konfiguration für Angular (HTML5-Routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Angular-Build ins nginx-Webroot kopieren
COPY --from=builder /build/dist/*/browser /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1