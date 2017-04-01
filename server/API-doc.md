# API documentation

### generic
> api : /
```
params : None

output : WhenBus-v${version-number}

description : Indicates API version-number
```

### Suggest Module
> api : /suggest/nearestStop
```
params :
{
  "coord" : {                           [*]
    "lat" : float,                      [*]
    "lng" : float                       [*]
  }
}

output :
{

}

```

> api : /suggest/bus
```
params :
{
  "src" : stop_id,                      [-]
  "dest" : stop_id                      [*]
}

output :
{

}

```
