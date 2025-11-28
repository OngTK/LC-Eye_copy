import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import store from './admin/store/store';
import { Provider } from 'react-redux';
import './assets/css/main.css';

// react-query 클라이언트는 애플리케이션 생애주기 동안 단일 인스턴스로 유지
const queryClient = new QueryClient();

const root = document.querySelector('#root');
const create = createRoot(root);

create.render(
    <QueryClientProvider client={queryClient}>
        <Provider store={store}>
            <App />
        </Provider>
    </QueryClientProvider>
);
