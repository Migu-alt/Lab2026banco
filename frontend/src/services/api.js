const BASE_URL = 'http://localhost:8080/api';

/**
 * Error de aplicación con un mensaje pensado para mostrarse directamente
 * al usuario (en vez de errores técnicos como "Failed to fetch").
 */
export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
  }
}

async function parseErrorMessage(response) {
  try {
    const text = await response.text();
    if (!text) return null;
    try {
      const json = JSON.parse(text);
      return json.message || json.error || (typeof json === 'string' ? json : null);
    } catch {
      // El backend a veces devuelve el mensaje como texto plano (String)
      return text;
    }
  } catch {
    return null;
  }
}

function messageForStatus(status, fallback) {
  switch (status) {
    case 400:
      return fallback || 'La solicitud contiene datos inválidos.';
    case 404:
      return fallback || 'El recurso solicitado no existe.';
    case 500:
      return 'Ocurrió un error interno en el servidor. Intenta nuevamente más tarde.';
    default:
      return fallback || `Ocurrió un error inesperado (código ${status}).`;
  }
}

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch (networkError) {
    throw new ApiError(
      'No se pudo conectar con el servidor. Verifica que el backend esté encendido en http://localhost:8080.'
    );
  }

  if (!response.ok) {
    const backendMessage = await parseErrorMessage(response);
    throw new ApiError(messageForStatus(response.status, backendMessage), response.status);
  }

  if (response.status === 204) {
    return null;
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export const api = {
  getCustomers: () => request('/customers'),
  getCustomerById: (id) => request(`/customers/${id}`),
  createCustomer: (customer) =>
    request('/customers', { method: 'POST', body: JSON.stringify(customer) }),

  transferMoney: (transfer) =>
    request('/transactions', { method: 'POST', body: JSON.stringify(transfer) }),
  getTransactionsByAccount: (accountNumber) =>
    request(`/transactions/${encodeURIComponent(accountNumber)}`),
  updateTransaction: (id, transaction) =>
    request(`/transactions/${id}`, { method: 'PUT', body: JSON.stringify(transaction) }),
  deleteTransaction: (id) => request(`/transactions/${id}`, { method: 'DELETE' }),
};
