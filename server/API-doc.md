# API documentation

### GENERIC

> GET /

**description :** *The API outputs the WhenBus API version number*

```
params : None

output : WhenBus-v${version-number}

description : Indicates API version-number
```

### SUGGEST Module

> POST /suggest/nearestStop

**description :** *The API is used to suggest the geographically nearest
bus stop to the user.*

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

**description :** *The API suggests possible bus numbers for given destination,
also suggesting the nearest starting bus stop too.*

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

**description :** *This API, returns the best possible bus [expected to arrive
the earliest] given the bus number and the direction of the bus*

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
  "id" : Number,                          # bus id ( use for feedback )
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

**description :** *This API is called when the user is ready to provide
valuable feedback, the server records the user and assigns an unique key, which
has to be used for subsequent feedback transactions*

```
params :
{
  "busNo" : string,                 [*]  # The bus user is in
  source : String,                  [*]  # Bus start point
  destination : String,             [*]  # Bus Destination
  "id" : Number,                    [*]  # id returned from info module
  "src" : string,                   [*]  # user start point
  "end" : string                    [*]  # user destination
}

output :
{
  "key" : string                   # Unique identifier for FPU
}
```

> POST /feedback/send

**description :** *This API will be pooled periodically by an Feedback Providing
User (FPU), where he send the stop he is headed to, also specifies the distance
to the stop headed towards and the measured velocity. The server drops the
user once he has reached his destination.*

```
params :
{
  "key" : string                [*] # FPU key
  "coord" : {                   [*] # Location of the user
    "lat" : float,
    "lng" : float
  },
  "distance" : float,           [*] # Distance to the stop mentioned
  "velocity" : float,           [*] # calculated velocity [between feedback's]
  "stop" : string               [*] # Stop the user is in or
                                    # the next stop user is heading towards
}

output :
{
  "status" : "OK"/"DROP"            # Accept/Drop Feedback
}
```
> POST /feedback/end

**description :** *The user can voluntarily call this API, to stop sending
feedback.*

```
params :
{
  "key" : string                 [*] # FPU key
}

output :                     # drop's the user if the record exists in DB
{
  "status" : "success"
}
```
### SYNC Module

> GET /sync/stops

**description :** *This API is called to get all the bus stops information
stored in the database*

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

**description :** *This API is called to get the information of all the buses
information stored in the database*

```
params : None

output :
[
  {
    "busNo" : string,               # Bus Number
    "source"   : string,            # Bus Starting Point
    "destination"  : string,        # Bus End Point
    "busStopList" : [string]        # List of Bus Stops the bus goes through
  }
]
                                    # All three make a key
```
