/**
 * Response Builder according to Antarang CAP Platform API Specification v1.0
 */

export const createSuccessResponse = (data = {}, message = "Request processed successfully") => {
  return {
    success: true,
    message,
    data,
    timestamp: new Date().toISOString()
  };
};

export const createPaginatedResponse = (
  content = [],
  page = 0,
  size = 20,
  totalElements = 0,
  message = "Records fetched successfully"
) => {
  const totalPages = Math.ceil(totalElements / size) || 1;
  return {
    success: true,
    message,
    data: {
      content,
      page,
      size,
      totalElements,
      totalPages,
      last: page >= totalPages - 1
    },
    timestamp: new Date().toISOString()
  };
};

export const createErrorResponse = (
  errorCode = "SYSTEM_ERROR",
  message = "An error occurred",
  details = [],
  path = "/api/v1"
) => {
  return {
    success: false,
    errorCode,
    message,
    details,
    timestamp: new Date().toISOString(),
    path
  };
};
