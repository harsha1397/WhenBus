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
  "coord" : {                           [*]
    "lat" : float,                      
    "lng" : float                       
  }
}

output :
{
  "distance": float,
  "stopName": string,
  "coord" : {
    "lat" : float,
    "lng" : float
  }
}

```

> POST /suggest/bus
```
params :
{
  "src" : stop_name,                    [-]
  "dest" : stop_name,                   [*]
  "coord" : {                           [*]
    "lat" : float,
    "lng" : float
  }
}

output :
[
  {
    "bus_no": string,
    "src"   : string,
    "time"  : float,
    "distance" : float                  [-]
  }
]

```

### INFO Module

> POST /info/bus
```
params :
{
  "bus_no" : string,                      [*]
  "start_point" : stop_name,              [*]
  "end_point" : stop_name,                [*]
  "coord" : {                             [*]
    "lat" : float,
    "lng" : float
  },
  "src" : stop_id,                        [-]
  "dest" : stop_id                        [-]
}

output :
{
  "stop" : stop_name,
  "busLoc" : {
    "lat" : float,
    "lng" : float
  },
  "time" : float
}

```

### FEEDBACK Module

> POST /feedback/send
```
params :
{

}

output :
{

}
```

### SYNC Module

> GET /sync/stops
```
params : None

output :
[
  {
    "id" : string,
    "name" : string,
    "coord" : {
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
    "busNo" : string,
    "src"   : stop_id,
    "dest"  : stop_id,
  }
]

```
