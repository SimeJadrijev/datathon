import {QueryClient, QueryClientProvider} from "@tanstack/react-query";
import {ErrorBoundary} from "react-error-boundary";
import {Router} from "@/Router.tsx";
import {AppLayout} from "@/layouts";
import {ErrorPage} from "@/pages";

const queryClient = new QueryClient();

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
