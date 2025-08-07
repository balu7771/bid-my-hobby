# Bid My Hobby

A Spring Boot application for hobby item bidding with Kafka-based notifications.

## Features

- **Item Upload & Bidding**: Upload hobby items and place bids
- **AI-Powered Moderation**: Automatic content moderation using OpenAI
- **Real-time Notifications**: Kafka-based event streaming for notifications
- **Image Processing**: Watermarking and image optimization
- **Payment Integration**: RazorPay payment processing
- **Email Notifications**: AWS SES integration

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker (for Kafka)

### 1. Start Kafka
```bash
./start-kafka.sh
```

### 2. Configure Environment
Create a `.env` file with your credentials:
```
OPENAI_API_KEY=your_openai_key
RAZORPAY_KEY_ID=your_razorpay_key
RAZORPAY_KEY_SECRET=your_razorpay_secret
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
```

### 3. Run Application
```bash
mvn spring-boot:run
```

### 4. Access Application
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Kafka UI**: http://localhost:8081

## API Endpoints

### Core Endpoints
- `GET /api/bid/allItems` - Get all items
- `POST /api/bid/uploadItem` - Upload new item
- `POST /api/bid/placeBid` - Place a bid
- `GET /api/bid/getBids` - Get bids for item

### Management
- `POST /api/bid/subscribe` - Subscribe to notifications
- `DELETE /api/bid/deleteItem/{itemId}` - Delete item
- `POST /api/bid/markAsSold/{itemId}` - Mark as sold

## Architecture

- **Backend**: Spring Boot 3.3.12
- **Messaging**: Apache Kafka
- **Storage**: AWS S3
- **Database**: PostgreSQL (metadata)
- **AI**: OpenAI GPT-4 Vision
- **Payments**: RazorPay
- **Email**: AWS SES

## Development

### Build
```bash
mvn clean package
```

### Test
```bash
mvn test
```

### Docker Build
```bash
docker build -t bid-my-hobby .
```

## Kafka Integration

The application uses Kafka for real-time notifications when items are uploaded. See [KAFKA_SETUP.md](KAFKA_SETUP.md) for detailed setup instructions.

## Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Push to branch
5. Create Pull Request