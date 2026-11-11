FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG VITE_API_BASE_URL=/api/v1
ARG VITE_TENANT=demo-escola
ARG VITE_GOOGLE_PSE_CX=e54b7047fc7e64e10
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_TENANT=$VITE_TENANT
ENV VITE_GOOGLE_PSE_CX=$VITE_GOOGLE_PSE_CX
RUN npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
