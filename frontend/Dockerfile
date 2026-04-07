FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
ARG VITE_API_URL=http://localhost:8080/api
ENV VITE_API_URL=$VITE_API_URL
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY nginx.railway.conf /etc/nginx/nginx.railway.conf
EXPOSE 80
CMD ["/bin/sh", "-c", "if [ -n \"$RAILWAY_ENVIRONMENT\" ]; then cp /etc/nginx/nginx.railway.conf /etc/nginx/conf.d/default.conf && envsubst '$PORT' < /etc/nginx/conf.d/default.conf > /tmp/default.conf && mv /tmp/default.conf /etc/nginx/conf.d/default.conf; fi && nginx -g 'daemon off;'"]
