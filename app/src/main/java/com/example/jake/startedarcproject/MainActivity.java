package com.example.jake.startedarcproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private FeatureLayer treeLayer;
    private Callout mCallout;

    private void addLayer(final ArcGISMap map, final String layerItem) {
        // addLayer (map, string)
        // where the map is an active map and the string is a reference to a location of an item on ArcGIS online server
        // This method adds the specified layer to the map by accessing arcGIS online's server
        Portal portal = new Portal("http://www.arcgis.com");
        PortalItem streetTrees = new PortalItem(portal, layerItem);
        treeLayer = new FeatureLayer(streetTrees, 0);
        map.getOperationalLayers().add(treeLayer);
    }

    private void addSwitchLayer(Switch switchx, String layerUrl, final ArcGISMap map){
        // adds a layer that is connected to a switch that can be turned off and on
        //(Switch, String, Map) -> Switch (Connected to Layer on Map)

        //Loads in the layer into the map
        ServiceFeatureTable table = new ServiceFeatureTable(layerUrl);
        final FeatureLayer toggleLayer = new FeatureLayer(table);
        map.getOperationalLayers().add(toggleLayer);

        //Activates the switch and listens for it's activity
        switchx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){ // Turn layer on if switch is on
                    toggleLayer.setVisible(true);
                } else { // turn layer off if switch is off
                    toggleLayer.setVisible(false);
                }
            }
        });
    }


    private void displayInformation (final ArcGISMap map, String queryTableUrl) {
        // Takes an ArcGISMap Map, and a reference to a hosted layer, and adds a queryable layer to the Map
        // object through a touch action. Returns the information from the user selected touch variable
        // in a double string array [][]
        //(Map, String) -> StringArray [][]

        mMapView.setOnTouchListener(new DefaultMapViewOnTouchListener(this, mMapView){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {

                // imports the table to be queried
                ServiceFeatureTable table = new ServiceFeatureTable(queryTableUrl);
                final FeatureLayer queryLayer = new FeatureLayer(table);
                map.getOperationalLayers().add(queryLayer);

                //get the point that was clicked and convert it to a map coordinate
                final Point clickPoint = mMapView
                        .screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));

                //create a selection tolerance
                int tolerance = 10;
                double mapTolerance = tolerance * mMapView.getUnitsPerDensityIndependentPixel();

                // use tolerance to create envelope to query
                Envelope envelope = new Envelope(clickPoint.getX() - mapTolerance, clickPoint.getY() - mapTolerance,
                        clickPoint.getX() + mapTolerance, clickPoint.getY() + mapTolerance, mMapView.getSpatialReference());
                QueryParameters query = new QueryParameters();
                query.setGeometry(envelope);

                //Request all attribute fields
                final ListenableFuture<FeatureQueryResult> future = table
                        .queryFeaturesAsync(query, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);

                future.addDoneListener(new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            // call get on the future to get the result
                            FeatureQueryResult result = future.get();

                            //convert the result to a transferable format using the putExtra command
                            //create iterator
                            Iterator<Feature> iterator = result.iterator();
                            //cycle through selections
                            int counter=0;

                            //create bundle to pass to second activity
                            ArrayList<String> fieldDetails = new ArrayList<String>();
                            ArrayList<String> valueDetails = new ArrayList<String>();

                            Feature feature;
                            while (iterator.hasNext()) {
                                feature = iterator.next();
                                // create a map of all available attributes as a name value pair
                                Map<String, Object> attr = feature.getAttributes();
                                Set<String> keys = attr.keySet();
                                for (String key : keys) {
                                    Object value = attr.get(key);
                                    //add values from FeatureQueryResult to list of strings
                                    fieldDetails.add(key);
                                    valueDetails.add(value + "");
                                }
                                counter++;
                            }

                            //now that result is obtained, send to DetailActivity
                            Intent showDetailsActivity = new Intent(getApplicationContext(),DetailsActivity.class);
                            showDetailsActivity.putExtra("ITEM_INDEX", fieldDetails);
                            showDetailsActivity.putExtra("VALUE_INDEX", valueDetails);
                            startActivity(showDetailsActivity);


                        }   catch (Exception e) {
                            Log.e(getResources().getString(R.string.app_name), "Select feature failed: " + e.getMessage());
                        } // end of catch and try
                    } // end of run
                }); // end of future.addDoneListener
                return super.onSingleTapConfirmed(e);
            }
        }); //end of listener
    } // end of displayInformation


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapView);
        Switch switch1 = (Switch) findViewById(R.id.layerSwitch1);
        Switch switch2 = (Switch) findViewById(R.id.layerSwitch2);
        Switch switch3 = (Switch) findViewById(R.id.layerSwitch3);
        Switch switch4 = (Switch) findViewById(R.id.layerSwitch4);

        if (mMapView != null) {
            Basemap.Type basemapType = Basemap.Type.LIGHT_GRAY_CANVAS;
            //set initial lat and long coordinates to center map on and level of detail
            double latitude = 43.726393;
            double longitude = -79.389279;
            int levelOfDetail = 11;

            //create the ArcGISMap object, map
            ArcGISMap map = new ArcGISMap(basemapType, latitude, longitude, levelOfDetail);

            //run the map with settings specified
            mMapView.setMap(map);

            //add static layers to the map
            //addLayer(map, getResources().getString(R.string.torontoParks));
            //addLayer(map, getResources().getString(R.string.torontoTrees));

            //add switch layers to the map
            addSwitchLayer(switch1, getResources().getString(R.string.torontoTreesUrl), map);
            addSwitchLayer(switch2, getResources().getString(R.string.torontoParksUrl), map);
            addSwitchLayer(switch3, getResources().getString(R.string.torontoBikesUrl), map);
            addSwitchLayer(switch4, getResources().getString(R.string.torontoTrailsUrl), map);

            //add queryable layer to the map
            displayInformation(map, getResources().getString(R.string.torontoNeighbourhoodUrl));
        }
    } // end of onCreate

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        mMapView.dispose();
        super.onDestroy();
    }

} // End of main activity
