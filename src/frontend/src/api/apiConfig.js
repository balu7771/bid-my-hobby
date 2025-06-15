// API configuration for the application
// This file can be expanded to include authentication tokens, headers, etc.

// Base URL for API requests - adjust as needed for your environment
const API_BASE_URL = '';  // Empty string means same origin

// Common headers for API requests
const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
};

// API endpoints
const ENDPOINTS = {
  ITEMS: '/api/bid/allItems',
  UPLOAD_ITEM: '/api/bid/uploadItem',
  PLACE_BID: '/api/bid/placeBid',
  DELETE_ITEM: (itemId) => `/api/bid/deleteItem/${itemId}`,
  MARK_AS_SOLD: (itemId) => `/api/bid/markAsSold/${itemId}`,
  SUBSCRIBE: '/api/bid/subscribe',
  HEALTH: '/api/health',
  HEALTH_STATUS: '/api/health/status',
  IMAGE: (itemId) => `/api/images/${itemId}`,
  GET_IMAGE_URL: (item) => item.url || `/api/images/${item.itemId}`,
  REQUEST_BID_ACCESS: (itemId) => `/api/bid/requestBidAccess/${itemId}`,
  VERIFY_AND_GET_BIDS: (token) => `/api/bid/verifyAndGetBids/${token}`,
  GET_BIDS: (itemId) => `/api/bid/getBids/${itemId}`,
};

export { API_BASE_URL, DEFAULT_HEADERS, ENDPOINTS };