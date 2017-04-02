# API documentation

### generic
> GET /
```
params : None

output : WhenBus-v${version-number}

description : Indicates API version-number
```

### Suggest Module
> POST /suggest/nearestStop
```
params :
{
  "coord" : {                           [*] # User Location
    "lat" : float,                      
    "lng" : float                       
  }
}

output :
{
  "distance": float,                        # Distance to the stop [in m]
  "stopName": string,                       # Stop Name
  "coord" : {                               # Stop Location
    "lat" : float,
    "lng" : float
  }
}

```

> POST /suggest/bus
```
params :
{
  "src" : stop_name,                    [-] # User needed source (optional)
  "dest" : stop_name,                   [*] # User destination
  "coord" : {                           [*] # User Location
    "lat" : float,
    "lng" : float
  }
}

output :
[
  {
    "bus_no": string,             # Bus Number suggested
    "src"   : string,             # boarding stop
    "time"  : float,              # Expected Time of arrival at stop [in min]
                                  # (measured from 00:00 )
    "distance" : float       [-]  # (optional -- iff src is not specified
  }                                             
]

```

### INFO Module

> POST /info/bus
```
params :
{
  "bus_no" : string,                      [*] # Bus Number
  "start_point" : stop_name,              [*] # Start Point of Bus
  "end_point" : stop_name,                [*] # End Point of Bus
  "coord" : {                             [*] # User Location
    "lat" : float,
    "lng" : float
  },
  "src" : stop_name,                      [-] # User specified source
  "dest" : stop_name                      [-] # User specified destination
}

output :
{
  "id" : bus_id,                          # bus id ( use for feedback )
  "stop" : stop_name,                     # Nearest Stop Name   
  "busLoc" : {                            # Estimated Location of Bus
    "lat" : float,  
    "lng" : float
  },
  "time" : float                          # Expected time of arrival of bus
}                                         # at the stop[in minutes from 00:00]

```

### FEEDBACK Module

> POST /feedback/access
```
params :
{
  "busNo" : string,                 [*]  # The bus user is in
  "id" : string,                    [*]  # id returned from info module
  "src" : string,                   [*]  # user start point
  "end" : string                    [*]  # user destination
}

output :
{
  "key" : string                   # Unique identifier for FPU
}
```

> POST /feedback/send
```
params :
{
  "key" : string                [*] # FPU key
  "coord" : {                   [-] # Location of the user
    "lat" : float,
    "lng" : float
  },
  "stop" : string               [-] # Stop the user is in
}

output :
{
  "status" : "OK"/"DROP"            # Accept/Drop Feedback
}
```

### SYNC Module

> GET /sync/stops
```
params : None

output :
[
  {
    "stopName" : string,            # Bus Stop Name (unique)
    "coord" : {                     # Stop Location
      "lat" : float,
      "lng" : float
    }
  }
]

```

> GET /sync/bus
```
params : None

output :
[
  {
    "busNo" : string,               # Bus Number
    "source"   : string,            # Bus Starting Point
    "destination"  : string,        # Bus End Point
  }
]
                                    # All three make a key
```
