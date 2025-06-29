// API configuration for the application
// This file can be expanded to include authentication tokens, headers, etc.

// Base URL for API requests - adjust as needed for your environment
// Empty string means same origin (which is what we want for all environments)
const API_BASE_URL = '';

// Common headers for API requests
const DEFAULT_HEADERS = {
  'Content-Type': 'application/json',
};

// API endpoints
const ENDPOINTS = {
  BASE: '/api/bid',
  ITEMS: '/api/bid/allItems',
  UPLOAD_ITEM: '/api/bid/uploadItem',
  PLACE_BID: '/api/bid/placeBid',
  DELETE_ITEM: (itemId) => `/api/bid/deleteItem/${itemId}`,
  MARK_AS_SOLD: (itemId) => `/api/bid/markAsSold/${itemId}`,
  MARK_NOT_FOR_SALE: (itemId) => `/api/bid/markNotForSale/${itemId}`,
  LIKE_ITEM: (itemId) => `/api/bid/likeItem/${itemId}`,
  UNLIKE_ITEM: (itemId) => `/api/bid/unlikeItem/${itemId}`,
  CREATOR_ITEMS: '/api/bid/creatorItems',
  SUBSCRIBE: '/api/bid/subscribe',
  HEALTH: '/api/health',
  HEALTH_STATUS: '/api/health/status',
  IMAGE: (itemId) => `/api/images/${itemId}`,
  GET_IMAGE_URL: (item) => item.url || `/api/images/${item.itemId}`,
  REQUEST_BID_ACCESS: (itemId) => `/api/bid/requestBidAccess/${itemId}`,
  VERIFY_AND_GET_BIDS: (token) => `/api/bid/verifyAndGetBids/${token}`,
  GET_BIDS: (itemId) => `/api/bid/getBids?itemId=${encodeURIComponent(itemId)}`,
  GET_ITEM_WITH_BIDS: (itemId) => `/api/bid/getItemWithBids?itemId=${encodeURIComponent(itemId)}`,
};

export { API_BASE_URL, DEFAULT_HEADERS, ENDPOINTS };