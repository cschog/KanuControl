import fn from '../../util/CookieUtil.js';
import sessionStorageHelper from './sessionStorageHelper.js';
import { interactionCodeFlow } from './interactionCodeFlow.js';
import { FORMS } from '../ion/RemediationConstants.js';
import transformIdxResponse from '../ion/transformIdxResponse.js';

function hasAuthenticationSucceeded(idxResponse) {
  var _idxResponse$rawIdxSt, _idxResponse$rawIdxSt2;

  // Check whether authentication has succeeded. This is done by checking the server response
  // and seeing if either the 'success' or 'successWithInteractionCode' objects are present.
  return (idxResponse === null || idxResponse === void 0 ? void 0 : (_idxResponse$rawIdxSt = idxResponse.rawIdxState) === null || _idxResponse$rawIdxSt === void 0 ? void 0 : _idxResponse$rawIdxSt.success) || (idxResponse === null || idxResponse === void 0 ? void 0 : (_idxResponse$rawIdxSt2 = idxResponse.rawIdxState) === null || _idxResponse$rawIdxSt2 === void 0 ? void 0 : _idxResponse$rawIdxSt2.successWithInteractionCode);
}
/**
  * When "Remember My Username" is enabled, we save the identifier in a cookie
  * so that the next time the user visits the SIW, the identifier field can be 
  * pre-filled with this value.
  */


function updateIdentifierCookie(appState, idxResponse) {
  const settings = appState.settings;

  if (settings.get('features.rememberMe')) {
    var _idxResponse$context;

    // Update the cookie with the identifier
    const user = idxResponse === null || idxResponse === void 0 ? void 0 : (_idxResponse$context = idxResponse.context) === null || _idxResponse$context === void 0 ? void 0 : _idxResponse$context.user;
    const {
      identifier: identifier
    } = (user === null || user === void 0 ? void 0 : user.value) || {};

    if (identifier) {
      fn.setUsernameCookie(identifier);
    }
  } else {
    // We remove the cookie explicitly if this feature is disabled.
    fn.removeUsernameCookie();
  }
}

async function updateAppState(appState, idxResponse) {
  const settings = appState.settings; // Only update the cookie when the user has successfully authenticated themselves 
  // to avoid incorrect/unnecessary updates.

  if (hasAuthenticationSucceeded(idxResponse) && settings.get('features.rememberMyUsernameOnOIE')) {
    updateIdentifierCookie(appState, idxResponse);
  }

  const lastResponse = appState.get('idx');
  const useInteractionCodeFlow = settings.get('useInteractionCodeFlow');

  if (useInteractionCodeFlow) {
    if (idxResponse.interactionCode) {
      // Although session.stateHandle isn't used by interation flow,
      // it's better to clean up at the end of the flow.
      sessionStorageHelper.removeStateHandle(); // This is the end of the IDX flow, now entering OAuth

      const tokens = await interactionCodeFlow(settings, idxResponse); // reset terminal view in case the were OAuth errors prior to successful token retrieval

      if (appState.get('currentFormName') === FORMS.TERMINAL) {
        appState.unset('currentFormName', {
          silent: true
        });
      }

      return tokens;
    }
  } else {
    // Do not save state handle for the first page loads.
    // Because there shall be no difference between following behavior
    // 1. bootstrap widget
    //    -> save state handle to session storage
    //    -> refresh page
    //    -> introspect using sessionStorage.stateHandle
    // 2. bootstrap widget
    //    -> do not save state handle to session storage
    //    -> refresh page
    //    -> introspect using options.stateHandle
    if (lastResponse) {
      var _idxResponse$context2;

      sessionStorageHelper.setStateHandle(idxResponse === null || idxResponse === void 0 ? void 0 : (_idxResponse$context2 = idxResponse.context) === null || _idxResponse$context2 === void 0 ? void 0 : _idxResponse$context2.stateHandle);
    } // Login flows that mimic step up (moving forward in login pipeline) via internal api calls,
    // need to clear stored stateHandles.
    // This way the flow can maintain the latest state handle. For eg. Device probe calls


    if (appState.get('currentFormName') === FORMS.CANCEL_TRANSACTION) {
      sessionStorageHelper.removeStateHandle();
    }
  } // transform response


  const ionResponse = transformIdxResponse(settings, idxResponse, lastResponse);
  await appState.setIonResponse(ionResponse);
}

export { updateAppState };
//# sourceMappingURL=updateAppState.js.map
