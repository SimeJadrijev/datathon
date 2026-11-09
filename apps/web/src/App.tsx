import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {ErrorBoundary} from "react-error-boundary";
import {Router} from "@/Router.tsx";
import {ApiError} from "@/api";
import {AppLayout} from "@/layouts";
import {ErrorPage} from "@/pages";

const MAX_RETRIES = 2;

const shouldRetry = (failureCount: number, error: Error) => {
    const isClientError = error instanceof ApiError && error.status !== undefined && error.status < 500;
    return !isClientError && failureCount < MAX_RETRIES;
};

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {retry: shouldRetry},
    },
});

function App() {

    return (
        <ErrorBoundary FallbackComponent={ErrorPage}>
            <QueryClientProvider client={queryClient}>
                <AppLayout>
                    <Router/>
                </AppLayout>
            </QueryClientProvider>
        </ErrorBoundary>
    )
}

export default App
