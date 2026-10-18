"""
Model Catalog and Evaluation Metrics Router.
"""
from typing import List, Dict, Any
from app.core.compat import APIRouter, HTTPException
from app.ml.evaluation import get_model_registry

router = APIRouter(prefix="/models", tags=["Model Registry"])

@router.get("")
def list_models() -> List[Dict[str, Any]]:
    """
    Retrieve registered ML models, performance metrics (MAE, RMSE, F1), and training metadata.
    """
    return get_model_registry()

@router.get("/{model_name}")
def get_model_by_name(model_name: str) -> Dict[str, Any]:
    """
    Retrieve metadata for a specific machine learning model.
    """
    models = get_model_registry()
    for m in models:
        if m.get("modelName") == model_name:
            return m
    raise HTTPException(status_code=404, detail=f"Model '{model_name}' not found in registry")
