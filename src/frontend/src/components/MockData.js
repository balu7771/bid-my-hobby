// Mock data for testing when backend is not available
export const mockItems = [
  {
    itemId: 'mock-item-1',
    name: 'Vintage Camera Collection',
    description: 'A collection of 5 vintage cameras from the 1960s in excellent condition.',
    userId: 'user123',
    email: 'creator1@example.com',
    basePrice: '299.99',
    currency: 'USD',
    status: 'ACTIVE',
    timestamp: Date.now() - 86400000 // 1 day ago
  },
  {
    itemId: 'mock-item-2',
    name: 'Handcrafted Wooden Chess Set',
    description: 'Beautiful hand-carved wooden chess set with inlaid board. Each piece is uniquely crafted.',
    userId: 'user456',
    email: 'creator2@example.com',
    basePrice: '150.00',
    currency: 'GBP',
    status: 'ACTIVE',
    timestamp: Date.now() - 172800000 // 2 days ago
  },
  {
    itemId: 'mock-item-3',
    name: 'Rare Stamp Collection',
    description: 'Collection of 50+ rare stamps from around the world, including some limited editions.',
    userId: 'user789',
    email: 'creator3@example.com',
    basePrice: '500.00',
    currency: 'INR',
    status: 'SOLD',
    timestamp: Date.now() - 259200000 // 3 days ago
  },
  {
    itemId: 'mock-item-4',
    name: 'Antique Pocket Watch',
    description: 'Beautiful gold-plated pocket watch from the 1920s, still in working condition.',
    userId: 'user123',
    email: 'creator1@example.com',
    basePrice: '199.99',
    currency: 'USD',
    status: 'DELETED',
    timestamp: Date.now() - 345600000 // 4 days ago
  }
];

// Function to get mock image URLs (for testing)
export const getMockImageUrl = (itemId) => {
  return `https://picsum.photos/seed/${itemId}/300/200`;
};