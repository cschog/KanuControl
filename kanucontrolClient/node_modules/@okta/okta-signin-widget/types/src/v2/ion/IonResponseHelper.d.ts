declare namespace _default {
    export { convertFormErrors };
    export { isIonErrorResponse };
    export { isIdxSessionExpiredError };
}
export default _default;
declare function convertFormErrors(response: any): {
    responseJSON: {
        errorCauses: any[];
        errorSummary: any;
        errorSummaryKeys: any;
        errorIntent: any;
    };
};
declare function isIonErrorResponse(response?: {}): any;
declare function isIdxSessionExpiredError(response: any): boolean;
//# sourceMappingURL=IonResponseHelper.d.ts.map