[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-24ddc0f5d75046c5622901739e7c5dd533143b0c8e959d652212380cedb1ea36.svg)](https://classroom.github.com/a/DLC4WqXm)
# {Oviedo Franco}

Template para TP DDS 2024 - Entrega 3

[Render](https://two024-tp-entrega-3-oviedofranc.onrender.com)

# Endpoints

# Heladeras

Operaciones relacionadas con heladeras.

## GET /heladeras/crearGenericas

Crea Heladeras genericas en la base de datos

## GET /heladeras/deleteAll

Borra todo lo de la base de datos

## POST /heladeras

Agregar una nueva heladera.

**Request Body:**
- **application/json**

**Example Value:**
```json
{
  "id": 0,
  "nombre": "string",
  "cantidadDeViandas": 0
}
```

## GET /heladeras/{heladeraId}
Obtener una heladera por su ID

**Request Body:**

-   Parameters: **heladeraId (integer, path) - ID de la heladera**

## POST /depositos
Depositar una vianda en una heladera

**Request Body:**

- **application/json**

**Example Value:**
```
{
  "heladeraId": 0,
  "codigoQR": "string"
}
```
## POST /retiros
Retirar una vianda de una heladera

**Request Body:**

- **application/json**

**Example Value:**
```
{
  "id": 0,
  "codigoQR": "string",
  "tarjeta": "string",
  "fechaRetiro": "2024-06-14T01:01:26Z",
  "heladeraId": 0
}
```
## POST /temperaturas
Registrar la temperatura de una heladera

**Request Body:**

- **application/json**

**Example Value:**
```
{
  "temperatura": 0,
  "heladeraId": 0,
  "fechaMedicion": "2024-06-13T20:14:26.437Z"
}
```

## GET /heladeras/{heladeraId}/temperaturas
Obtener las temperaturas registradas de una heladera por su ID

**Request:**

-   Parameters: **heladeraId (integer, path) - ID de la heladera**

**Example Value:**
```
[
  {
    "temperatura": 0,
    "heladeraId": 0,
    "fechaMedicion": "2024-06-13T20:14:26.438Z"
  }
]
```


## Schemas

## HeladeraDTO
```json
{
  "id": "integer",
  "nombre": "string",
  "cantidadDeViandas": "integer"
}
```
-  **id (INTEGER)** - ID de la heladera, no usar en el POST!
nombre (string) 
- **Nombre de la heladera**  
- **Cantidad de viandas en la heladera (INTEGER)** 

## TemperaturaDTO
```json
{
  "temperatura": "integer",
  "heladeraId": "integer",
  "fechaMedicion": "string"
}
```

-  **temperatura (INTEGER)** - Temperatura registrada
- **heladeraId (STRING)** ID de la heladera 
- **fechaMedicion (STRING)** Fecha y hora de la medición 

## TemperaturaDTO
```json
{
  "id": "integer",
  "codigoQR": "string",
  "tarjeta": "string",
  "fechaRetiro": "string",
  "heladeraId": "integer"
}

```
- **id (INTEGER)** - ID del retiro (no usar en el POST)
- **codigoQR (STRING)** - Código QR de la vianda retirada
- **tarjeta (STRING)** - Tarjeta asociada al retiro
- **fechaRetiro (STRING)** - Fecha y hora del retiro
- **heladeraId (INTEGER)** - ID de la heladera
