package com.viettel.nims.optimalDesign.service.v2.suggestion.core.state;

import com.viettel.nims.optimalDesign.entity.Suggestion;

import java.lang.reflect.InvocationTargetException;

public interface SgtState {
  void updateSuggestion(int preState, String action, Suggestion suggestion) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

  int getSuggestionStatus();

}
