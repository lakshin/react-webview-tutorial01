/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View, Dimensions} from 'react-native';
import AdvancedWebView from './AdvancedWebview.android';

type Props = {};
var {height, width} = Dimensions.get('window');
export default class App extends Component<Props> {
  render() {
    return (
      <View style={styles.container}>
        <AdvancedWebView
          source={{ uri: "https://lakshinkarunaratne.wordpress.com/"}}
          style={{ flex: 1,width,height}}
          enabledUploadAndroid = {true}
        />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  }
});
