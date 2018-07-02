// @flow
import React from 'react';
import { connect } from 'react-redux';

import { fetchRepositoriesIfNeeded } from '../modules/page';
import Login from '../Login';


type Props = {
  loading: boolean,
  error: any,
  repositories: any,
  fetchRepositoriesIfNeeded: () => void
}

class Page extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchRepositoriesIfNeeded();
  }

  render() {
    const { loading, error, repositories } = this.props;


    if(loading) {
      return (
        <div>
          <h1>SCM</h1>
          <Login/>
        </div>
      );
    }
    else if(!loading){
      return (
        <div>
          <h1>SCM</h1>
          <h2>Startpage</h2>
          <a href={"/users" }>
            Users hier!
          </a>
        </div>
      );
    }


  }

}

const mapStateToProps = (state) => {
  return null;
};

const mapDispatchToProps = (dispatch) => {
  return {
    fetchRepositoriesIfNeeded: () => {
      dispatch(fetchRepositoriesIfNeeded())
    }
  }
};

export default connect(mapStateToProps, mapDispatchToProps)(Page);
