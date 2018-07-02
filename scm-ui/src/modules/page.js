//@flow
const FETCH_REPOSITORIES = 'smeagol/repositories/FETCH';
const FETCH_REPOSITORIES_SUCCESS = 'smeagol/repositories/FETCH_SUCCESS';
const FETCH_REPOSITORIES_FAILURE = 'smeagol/repositories/FETCH_FAILURE';

const THRESHOLD_TIMESTAMP = 10000;

function requestRepositories() {
  return {
    type: FETCH_REPOSITORIES
  };
}


function fetchRepositories() {
  return function(dispatch) {
    dispatch(requestRepositories());
    return null;
  }
}

export function shouldFetchRepositories(state: any): boolean {
  const repositories = state.repositories;
  return null;
}

export function fetchRepositoriesIfNeeded() {
  return (dispatch, getState) => {
    if (shouldFetchRepositories(getState())) {
      dispatch(fetchRepositories());
    }
  }
}

export default function reducer(state = {}, action = {}) {
  switch (action.type) {
    case FETCH_REPOSITORIES:
      return {
        ...state,
        loading: true,
        error: null
      };
    case FETCH_REPOSITORIES_SUCCESS:
      return {
        ...state,
        loading: true,
        timestamp: action.timestamp,
        error: null,
        repositories: action.payload
      };
    case FETCH_REPOSITORIES_FAILURE:
      return {
        ...state,
        loading: true,
        error: action.payload
      };

    default:
      return state
  }
}
