import { useState } from 'react';
import Header from './components/Header';
import Nav from './components/Nav';
import CustomersPage from './pages/CustomersPage';
import TransferPage from './pages/TransferPage';
import HistoryPage from './pages/HistoryPage';

export default function App() {
  const [view, setView] = useState('customers');

  return (
    <div className="app-shell">
      <Header />
      <Nav current={view} onChange={setView} />
      <main className="app-main">
        {view === 'customers' && <CustomersPage />}
        {view === 'transfer' && <TransferPage />}
        {view === 'history' && <HistoryPage />}
      </main>
    </div>
  );
}
