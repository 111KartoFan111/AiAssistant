# Используем Node 20 (Alpine)
FROM node:20-alpine

WORKDIR /app

# Устанавливаем зеркало npm для стабильной сети
RUN npm config set registry https://registry.npmmirror.com \
    && npm config set fetch-retries 5 \
    && npm config set fetch-retry-factor 2 \
    && npm config set fetch-timeout 60000

# Копируем package.json и package-lock.json
COPY package*.json ./

# Устанавливаем зависимости
RUN npm install

# Копируем остальные файлы проекта
COPY . .

# Сборка приложения
RUN npm run build

EXPOSE 5173
CMD ["npm", "run", "dev"]
