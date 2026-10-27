import {Route, Switch} from 'wouter';
import {routes} from '@/routes';
import {
    FundingCallDetailPage,
    FundingCallsPage,
    HomePage,
    NotFoundPage,
    RecommendationsPage,
    RegulationDetailPage,
    RegulationsPage,
    SourcesPage,
} from "@/pages";


export const Router = () => {
    return (
        <Switch>
            <Route path={routes.HOME} component={HomePage}/>
            <Route path={routes.RECOMMENDATIONS} component={RecommendationsPage}/>
            <Route path={routes.FUNDING_CALLS} component={FundingCallsPage}/>
            <Route path={routes.FUNDING_CALL_DETAIL} component={FundingCallDetailPage}/>
            <Route path={routes.REGULATIONS} component={RegulationsPage}/>
            <Route path={routes.REGULATION_DETAIL} component={RegulationDetailPage}/>
            <Route path={routes.SOURCES} component={SourcesPage}/>
            <Route component={NotFoundPage}/>
        </Switch>
    );
};
