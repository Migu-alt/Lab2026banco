export default function StatusMessage({ type = 'info', children }) {
  if (!children) return null;
  return (
    <div className={`status status--${type}`} role={type === 'error' ? 'alert' : 'status'}>
      {children}
    </div>
  );
}
