import { useState } from 'react';
import { api } from '../services/api';
import StatusMessage from '../components/StatusMessage';

const EMPTY_FORM = { senderAccountNumber: '', receiverAccountNumber: '', amount: '' };

export default function TransferPage() {
  const [form, setForm] = useState(EMPTY_FORM);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  function updateField(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function validate() {
    if (!form.senderAccountNumber.trim() || !form.receiverAccountNumber.trim()) {
      return 'La cuenta origen y la cuenta destino son obligatorias.';
    }
    if (form.senderAccountNumber.trim() === form.receiverAccountNumber.trim()) {
      return 'La cuenta origen y destino no pueden ser la misma.';
    }
    const amount = Number(form.amount);
    if (!form.amount || Number.isNaN(amount) || amount <= 0) {
      return 'El monto debe ser un número positivo.';
    }
    return '';
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setSuccess('');
    setError('');

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setSubmitting(true);
    try {
      const result = await api.transferMoney({
        senderAccountNumber: form.senderAccountNumber.trim(),
        receiverAccountNumber: form.receiverAccountNumber.trim(),
        amount: Number(form.amount),
      });
      setSuccess(
        `Transferencia realizada correctamente${result?.id ? ` (transacción #${result.id})` : ''}.`
      );
      setForm(EMPTY_FORM);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className="view">
      <div className="view__intro">
        <h1>Realizar transferencia</h1>
        <p>Envía dinero de una cuenta a otra. Los saldos se actualizan de inmediato.</p>
      </div>

      <form className="card form" onSubmit={handleSubmit}>
        <div className="field">
          <label htmlFor="sender">Cuenta origen</label>
          <input
            id="sender"
            type="text"
            value={form.senderAccountNumber}
            onChange={(e) => updateField('senderAccountNumber', e.target.value)}
            placeholder="Ej: 123456789"
            disabled={submitting}
          />
        </div>

        <div className="field">
          <label htmlFor="receiver">Cuenta destino</label>
          <input
            id="receiver"
            type="text"
            value={form.receiverAccountNumber}
            onChange={(e) => updateField('receiverAccountNumber', e.target.value)}
            placeholder="Ej: 987654321"
            disabled={submitting}
          />
        </div>

        <div className="field">
          <label htmlFor="amount">Monto</label>
          <input
            id="amount"
            type="number"
            min="0"
            step="0.01"
            value={form.amount}
            onChange={(e) => updateField('amount', e.target.value)}
            placeholder="0.00"
            disabled={submitting}
          />
        </div>

        <StatusMessage type="error">{error}</StatusMessage>
        <StatusMessage type="success">{success}</StatusMessage>

        <button type="submit" className="button" disabled={submitting}>
          {submitting ? 'Enviando…' : 'Transferir'}
        </button>
      </form>
    </section>
  );
}
