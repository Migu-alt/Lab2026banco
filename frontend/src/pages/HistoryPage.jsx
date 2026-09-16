import { useEffect, useState } from 'react';
import { api } from '../services/api';
import StatusMessage from '../components/StatusMessage';

const money = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'USD' });

function formatTimestamp(value) {
  if (!value) return '—';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('es-CO');
}

export default function HistoryPage() {
  const [customers, setCustomers] = useState([]);
  const [customersError, setCustomersError] = useState('');
  const [selectedAccount, setSelectedAccount] = useState('');
  const [manualAccount, setManualAccount] = useState('');

  const [transactions, setTransactions] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .getCustomers()
      .then((data) => setCustomers(Array.isArray(data) ? data : []))
      .catch((err) => setCustomersError(err.message));
  }, []);

  async function loadHistory(accountNumber) {
    const trimmed = accountNumber.trim();
    if (!trimmed) {
      setError('Selecciona un cliente o escribe un número de cuenta.');
      return;
    }
    setLoading(true);
    setError('');
    setTransactions(null);
    try {
      const data = await api.getTransactionsByAccount(trimmed);
      setTransactions(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function handleSelectCustomer(event) {
    const accountNumber = event.target.value;
    setSelectedAccount(accountNumber);
    setManualAccount('');
    if (accountNumber) {
      loadHistory(accountNumber);
    } else {
      setTransactions(null);
    }
  }

  function handleManualSubmit(event) {
    event.preventDefault();
    setSelectedAccount('');
    loadHistory(manualAccount);
  }

  return (
    <section className="view">
      <div className="view__intro">
        <h1>Histórico de transacciones</h1>
        <p>Consulta todas las transferencias enviadas o recibidas por una cuenta.</p>
      </div>

      <div className="card">
        <div className="field">
          <label htmlFor="customer-select">Elegir cliente</label>
          <select id="customer-select" value={selectedAccount} onChange={handleSelectCustomer}>
            <option value="">— Selecciona un cliente —</option>
            {customers.map((c) => (
              <option key={c.id} value={c.accountNumber}>
                {c.firstName} {c.lastName} — {c.accountNumber}
              </option>
            ))}
          </select>
          {customersError && <StatusMessage type="error">{customersError}</StatusMessage>}
        </div>

        <p className="form__divider">o</p>

        <form className="field-inline" onSubmit={handleManualSubmit}>
          <div className="field">
            <label htmlFor="manual-account">Número de cuenta</label>
            <input
              id="manual-account"
              type="text"
              value={manualAccount}
              onChange={(e) => setManualAccount(e.target.value)}
              placeholder="Ej: 123456789"
            />
          </div>
          <button type="submit" className="button button--secondary" disabled={loading}>
            Consultar
          </button>
        </form>
      </div>

      {loading && <StatusMessage type="info">Cargando histórico…</StatusMessage>}
      {!loading && error && <StatusMessage type="error">{error}</StatusMessage>}
      {!loading && !error && transactions && transactions.length === 0 && (
        <StatusMessage type="info">Esta cuenta no tiene transacciones registradas.</StatusMessage>
      )}

      {!loading && !error && transactions && transactions.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Fecha / hora</th>
                <th>Cuenta origen</th>
                <th>Cuenta destino</th>
                <th>Monto</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx) => (
                <tr key={tx.id}>
                  <td>{tx.id}</td>
                  <td>{formatTimestamp(tx.timestamp)}</td>
                  <td className="mono">{tx.senderAccountNumber}</td>
                  <td className="mono">{tx.receiverAccountNumber}</td>
                  <td className="mono">
                    {typeof tx.amount === 'number' ? money.format(tx.amount) : tx.amount}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
