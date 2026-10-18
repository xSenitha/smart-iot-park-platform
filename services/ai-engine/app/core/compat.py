"""
Compatibility layer for FastAPI and Pydantic when running in lean or test environments
without third-party binary wheels. Real libraries are loaded whenever available.
"""
from typing import Any, Dict, List, Optional, Callable

try:
    from pydantic import BaseModel as _PydanticBaseModel, Field as _PydanticField
    BaseModel = _PydanticBaseModel
    Field = _PydanticField
    HAS_PYDANTIC = True
except ImportError:
    HAS_PYDANTIC = False

    class BaseModel:
        def __init__(self, **kwargs):
            # Set default values from class annotations / class attributes
            for key, val in self.__class__.__dict__.items():
                if not key.startswith("_") and not callable(val):
                    setattr(self, key, val)
            for k, v in kwargs.items():
                setattr(self, k, v)

        def model_dump(self, *args, **kwargs) -> Dict[str, Any]:
            res = {}
            for k, v in self.__dict__.items():
                if not k.startswith("_"):
                    if isinstance(v, BaseModel):
                        res[k] = v.model_dump()
                    elif isinstance(v, list):
                        res[k] = [item.model_dump() if isinstance(item, BaseModel) else item for item in v]
                    else:
                        res[k] = v
            return res

        def dict(self, *args, **kwargs) -> Dict[str, Any]:
            return self.model_dump()

        @classmethod
        def model_validate(cls, obj: Any):
            if isinstance(obj, dict):
                return cls(**obj)
            return obj

    def Field(default: Any = ..., **kwargs) -> Any:
        return None if default is Ellipsis else default

try:
    from fastapi import FastAPI as _FastAPI, APIRouter as _APIRouter, HTTPException as _HTTPException
    from fastapi.middleware.cors import CORSMiddleware as _CORSMiddleware
    from fastapi.responses import JSONResponse as _JSONResponse
    FastAPI = _FastAPI
    APIRouter = _APIRouter
    HTTPException = _HTTPException
    CORSMiddleware = _CORSMiddleware
    JSONResponse = _JSONResponse
    HAS_FASTAPI = True
except ImportError:
    HAS_FASTAPI = False

    class HTTPException(Exception):
        def __init__(self, status_code: int = 500, detail: str = ""):
            super().__init__(detail)
            self.status_code = status_code
            self.detail = detail

    class JSONResponse:
        def __init__(self, content: Any = None, status_code: int = 200, **kwargs):
            self.content = content
            self.status_code = status_code

    class CORSMiddleware:
        pass

    class APIRouter:
        def __init__(self, prefix: str = "", tags: Optional[List[str]] = None):
            self.prefix = prefix
            self.tags = tags or []
            self.routes: List[Dict[str, Any]] = []

        def get(self, path: str, response_model: Any = None, **kwargs) -> Callable:
            def decorator(f: Callable) -> Callable:
                self.routes.append({"method": "GET", "path": self.prefix + path, "func": f, "response_model": response_model})
                return f
            return decorator

        def post(self, path: str, response_model: Any = None, **kwargs) -> Callable:
            def decorator(f: Callable) -> Callable:
                self.routes.append({"method": "POST", "path": self.prefix + path, "func": f, "response_model": response_model})
                return f
            return decorator

    class FastAPI:
        def __init__(self, title: str = "FastAPI", version: str = "1.0.0", lifespan: Any = None, docs_url: str = "/docs"):
            self.title = title
            self.version = version
            self.lifespan = lifespan
            self.docs_url = docs_url
            self.routers: List[APIRouter] = []
            self.routes: List[Dict[str, Any]] = []

        def include_router(self, router: APIRouter, **kwargs) -> None:
            self.routers.append(router)

        def add_middleware(self, middleware_class: Any, **kwargs) -> None:
            pass

        def get(self, path: str, response_model: Any = None, **kwargs) -> Callable:
            def decorator(f: Callable) -> Callable:
                self.routes.append({"method": "GET", "path": path, "func": f, "response_model": response_model})
                return f
            return decorator

        def post(self, path: str, response_model: Any = None, **kwargs) -> Callable:
            def decorator(f: Callable) -> Callable:
                self.routes.append({"method": "POST", "path": path, "func": f, "response_model": response_model})
                return f
            return decorator
