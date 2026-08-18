FROM nginx:alpine

COPY . /usr/share/nginx/html
COPY config.template.js /opt/sandra-x-andreia/config.template.js
COPY docker-entrypoint.d/40-generate-config.sh /docker-entrypoint.d/40-generate-config.sh

RUN sed -i 's/\r$//' /docker-entrypoint.d/40-generate-config.sh \
    && chmod +x /docker-entrypoint.d/40-generate-config.sh

EXPOSE 80
