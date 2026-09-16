import { useEffect, useState } from 'react';
import { api } from '../services/api';
import StatusMessage from '../components/StatusMessage';

const money = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'USD' });

export default function CustomersPage() {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    api
      .getCustomers()
      .then((data) => {
        if (active) setCustomers(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (active) setError(err.message);
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <section className="view">
      <div className="view__intro">
        <h1>Clientes</h1>
        <p>Listado de clientes registrados y el saldo actual de cada cuenta.</p>
      </div>

      {loading && <StatusMessage type="info">Cargando clientes…</StatusMessage>}
      {!loading && error && <StatusMessage type="error">{error}</StatusMessage>}
      {!loading && !error && customers.length === 0 && (
        <StatusMessage type="info">Todavía no hay clientes registrados.</StatusMessage>
      )}

      {!loading && !error && customers.length > 0 && (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Apellido</th>
                <th>Número de cuenta</th>
                <th>Saldo</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.id}</td>
                  <td>{customer.firstName}</td>
                  <td>{customer.lastName}</td>
                  <td className="mono">{customer.accountNumber}</td>
                  <td className="mono">
                    {typeof customer.balance === 'number' ? money.format(customer.balance) : '—'}
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
