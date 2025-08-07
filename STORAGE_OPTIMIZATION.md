# Storage Optimization: S3 + OCI Database

## Recommended Architecture

**Images**: AWS S3 (most cost-effective for files)
**Metadata**: OCI MySQL Database (free tier)
**Application**: OCI Compute (free tier)
**DNS**: AWS Route 53

## Cost Analysis

### Image Storage Options
| Service | Cost/GB/month | Pros | Cons |
|---------|---------------|------|------|
| AWS S3 | $0.023 | Reliable, CDN integration | - |
| OCI Object Storage | $0.0255 | Slightly cheaper | Less ecosystem |
| MongoDB GridFS | $0.25+ | Built-in metadata | 10x more expensive |

### Database Options for Metadata
| Service | Cost/month | Storage | Pros |
|---------|------------|---------|------|
| OCI MySQL (Free) | $0 | 20GB | Free, reliable |
| AWS RDS | $15-25 | 20GB | Managed, but costly |
| MongoDB Atlas | $9+ | 512MB | NoSQL, but limited free tier |

## Implementation Steps

### 1. Set up OCI MySQL Database (Free Tier)
```bash
# Create MySQL instance in OCI console
# Connection string: jdbc:mysql://your-oci-mysql:3306/bidmyhobby
```

### 2. Update application.properties
```properties
# Database configuration
spring.datasource.url=jdbc:mysql://your-oci-mysql:3306/bidmyhobby
spring.datasource.username=your-username
spring.datasource.password=your-password
spring.jpa.hibernate.ddl-auto=update

# Keep S3 configuration
aws.s3.bucket=bid-my-hobby
aws.s3.region=ap-south-1
```

### 3. Add MySQL dependency to pom.xml
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## Why This Hybrid Approach?

### Keep S3 for Images Because:
- **Cost**: $0.023/GB vs $0.25/GB for MongoDB
- **Performance**: Built for file storage
- **CDN Integration**: Easy CloudFront setup later
- **Reliability**: 99.999999999% durability

### Use OCI Database for Metadata Because:
- **Free**: 20GB storage, 1 OCPU
- **Performance**: Optimized for structured queries
- **Relationships**: Easy to link with user data
- **Backup**: Automated backups included

## Monthly Cost Estimate

| Component | Cost |
|-----------|------|
| AWS Route 53 | $1 |
| AWS S3 (10GB images) | $0.23 |
| OCI Compute | $0 (free tier) |
| OCI MySQL | $0 (free tier) |
| **Total** | **$1.23/month** |

Compare to full AWS: $25-40/month

## Migration Strategy

If you want to move images to OCI later:
1. Set up OCI Object Storage
2. Create a migration script
3. Update S3 URLs to OCI URLs
4. Save additional $0.03/GB/month

But for now, keeping S3 is the simplest and most reliable option.